package com.github.biconou.AudioPlayer;

/* libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2000,2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.SourceDataLine;

import org.slf4j.LoggerFactory;




/** 
 * Play.java
 * <p>
 * A simple class that plays audio from given file names.
 * <p>
 * Uses the Java Sound SourceDataLine interface to stream the sound. 
 * Converts compressed encodings (ALAW, ULAW, MP3) to PCM.
 * @author Dan Becker, beckerdo@io.com
 */
public class MPlayerPlayer implements Player {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MPlayerPlayer.class);

	
	private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);


	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#getState()
	 */
	public State getState() {
		return state.get();
	}


	PlayList playList = null;
	Thread audioStreamWriterThread = null;

	SourceDataLine dataLine = null;

	BufferedMultipleAudioInputStream bufferedStream = null;

	List<PlayerListener> listeners = new ArrayList<PlayerListener>();

	private  BufferedOutputStream cmdStream = null;
	//private  BufferedReader MplayerIStream = null;

	private String mPlayerCommand = null;
	private String tmpDir = null;
	List<String> tmpFiles = new ArrayList<String>();
	private Process mplayerProcess;


	// Mplayer stdout treatment
	List<String> mPlayerStandardOutputBuffer = null;	
	private Thread tTreatOutputMplayer = null;
	private Thread tReadOutputMplayer = null; 

	

	/**
	 * Public constructor.
	 * 
	 * @throws Exception
	 */
	public MPlayerPlayer(Float initialVolume) throws Exception {
		mPlayerCommand = System.getProperty("com.github.biconou.AudioPlayer.MPlayerPlayer.command");

		tmpDir = System.getProperty("com.github.biconou.AudioPlayer.MPlayerPlayer.tmpDir");

		init(initialVolume);
	}

	/**
	 * Initialisation of the audio player.
	 * A new mplayer process is created.
	 * 
	 * @throws IOException
	 */
	private void init(Float initialVolume) throws IOException,Exception {

		LOG.debug("Begin init()");
		
		if (mPlayerCommand == null) {
			Exception ex = new IllegalStateException("Undefined MPlayer command.");
			LOG.error("",ex);
			throw ex;
		}

		if (tmpDir == null) {
			Exception ex = new IllegalStateException("Undefined tmpDir.");
			LOG.error("",ex);
			throw ex;
		}
		
		File tmpDirFile = new File(tmpDir);
		if (!tmpDirFile.exists() || !tmpDirFile.isDirectory()) {
			Exception ex =  new IllegalStateException("tmpDir "+tmpDir+" does not exists or is not a directory");
			LOG.error("",ex);
			throw ex;
		} else {
			LOG.debug("Clean tmp directory");
			
			File[] fileList = tmpDirFile.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File oneFile = fileList[i];
				if (!oneFile.delete()) {
					LOG.warn("File {} has not been deleted !",oneFile.getName());
				}
				
			}
		}
		

		//ProcessBuilder pb = new ProcessBuilder(mPlayerCommand,"-slave","-idle","--gapless-audio","-v");
		String volumeOption = "";
		if (initialVolume != null) {
			volumeOption = "-volume "+(int) (initialVolume.floatValue()*100);
		}
		//ProcessBuilder pb = new ProcessBuilder(mPlayerCommand,"-slave","-idle","--gapless-audio",volumeOption);
		ProcessBuilder pb = new ProcessBuilder(mPlayerCommand,"-slave","-idle","--gapless-audio");
		LOG.debug("Start a new process : {}",pb.command());
		mplayerProcess = pb.start();
		OutputStream MplayerOStream = mplayerProcess.getOutputStream();
		cmdStream = new BufferedOutputStream(MplayerOStream);


		final  BufferedReader MplayerIStream = new BufferedReader(new InputStreamReader(mplayerProcess.getInputStream()));
		final  BufferedReader MplayerErrorStream = new BufferedReader(new InputStreamReader(mplayerProcess.getErrorStream()));


		mPlayerStandardOutputBuffer = new ArrayList<String>();

		//
		// Tread MPlayer standard output
		//
		LOG.debug("Launch the tTreatOutputMplayer thread");
		tTreatOutputMplayer = new Thread(new Runnable() {

			public void run() {
				
				String nowPlaying = null;
				
				while (mPlayerStandardOutputBuffer != null) {
					if (mPlayerStandardOutputBuffer.size() > 0) {
						String readLine = null;
						synchronized (mPlayerStandardOutputBuffer) {
							readLine = mPlayerStandardOutputBuffer.remove(0);							
						}
						LOG.debug("Read line in mPlayerStandardOutputBuffer : {}",readLine);
						if (readLine != null) {
							if (readLine.startsWith("Playing")) {
								LOG.debug("removed line 'Playing' from mPlayerStandardOutputBuffer");
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// Nothing to do
								}
								String wasPlaying = nowPlaying;
								nowPlaying = readLine;
								if (wasPlaying != null) {
									LOG.debug("invoque notifyNextStream();");
									notifyNextStream();
								}
							}
						}
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// Nothing to do
					}
					
				}
			}
		}); 

		//tTreatOutputMplayer.setPriority(Thread.MIN_PRIORITY);
		tTreatOutputMplayer.start();
		//
		 
		
		

		// Read MPlayer standard output
		LOG.debug("Launch the tReadOutputMplayer thread");
		tReadOutputMplayer = new Thread(new Runnable() {

			public void run() {

				String readLine = null;
				String nowPlaying = null;
				try {
					
					readLine = MplayerIStream.readLine();
					while (MplayerIStream != null && readLine != null) {
						if (!readLine.startsWith("A:")) {
							LOG.trace("MPLAYER_OUTPUT | {}",readLine);
						}
						if (readLine.startsWith("Playing")) {
							LOG.debug("Line 'Playing' detected");
							
						/*	String wasPlaying = nowPlaying;
							nowPlaying = readLine;
							LOG.debug("wasPlaying = {}",wasPlaying);
							LOG.debug("nowPlaying = {}",nowPlaying);
							if (wasPlaying != null) {
								LOG.debug("invoque notifyNextStream();");
								notifyNextStream();
							}
							*/
							
							if (mPlayerStandardOutputBuffer != null) {
								LOG.debug("Add line 'Playing' to mPlayerStandardOutputBuffer");
								synchronized (mPlayerStandardOutputBuffer) {
									mPlayerStandardOutputBuffer.add(readLine);									
								}
							} 
						}
						// read next line
						if (MplayerIStream != null) {
							readLine = MplayerIStream.readLine();
						}
					}
				}
				catch (IOException e) {
					LOG.error("Error while reading mplayer stdout",e);
				}
			}
		});

		tReadOutputMplayer.start();


		// Read MPlayer standard error
		Thread tErrorMplayer = new Thread(new Runnable() {

			public void run() {
				String readLine;
				try {
					readLine = MplayerErrorStream.readLine();
					while (readLine != null) {	            	  
						LOG.trace("MPLAYER_ERROR | {}",readLine);
						// read next line
						readLine = MplayerErrorStream.readLine();
					}
				}
				catch (IOException e) {
					LOG.error("Error while reading mplayer stderr",e);
				}
			}
		});

		tErrorMplayer.start();



	}

	/** 
	 * Plays audio from given file names.
	 * @param args Command line parameters
	 * @throws Exception 
	 */
	public static void main(String [] args) throws Exception {
		// Process arguments.
		try {
			playAudioFiles(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // main


	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#setPlayList(com.github.biconou.AudioPlayer.PlayList)
	 */
	public void setPlayList(PlayList playList) {
		this.playList = playList;
	}

	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#registerListener(com.github.biconou.AudioPlayer.PlayerListener)
	 */
	public void registerListener(PlayerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Sends a command to the MPlayer process. 
	 * 
	 * @param command The command to send.
	 * @throws IOException In case of error while sending the command to the process.
	 */
	private void sendCommandToMPlayer(String command) throws IOException {
		LOG.debug("Sending command to mplayer process : {}",command);
		if (!command.endsWith("\n")) {
			command = command + "\n";
		}
		cmdStream.write(command.getBytes("UTF-8"));
		cmdStream.flush();
	}


	/** 
	 * Play audio from the given file name. 
	 * @param fileName  The file to play
	 * @throws IOException 
	 */
	public static void playAudioFiles(String[] fileName) throws Exception {

		PlayList playList = new ArrayPlayList(fileName); 

		//Player player = new MPlayerPlayer("D:\\AudioPlayer\\mplayer2-x86_64-latest\\mplayer2.exe");
		Player player = new MPlayerPlayer(new Float((float)1.0));
		player.setPlayList(playList);
		try {
			player.play();
		} catch (NothingToPlayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 


	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#stop()
	 */
	public void stop() {
		try {
			state.set(State.CLOSED);
			sendCommandToMPlayer("stop");
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e);
			LOG.error("",ex);
			throw ex;
		}
	}

	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#pause()
	 */
	public void pause() {
		try {
			sendCommandToMPlayer("pause");
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e);
			LOG.error("",ex);
			throw ex;
		}
		state.set(State.PAUSED);
	}

	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#play()
	 */
	public void play() throws NothingToPlayException {

		if (getState().equals(State.PAUSED)) {
			try {
				sendCommandToMPlayer("pause");
			} catch (IOException e) {
				RuntimeException ex = new RuntimeException(e);
				LOG.error("",ex);
				throw ex;
			}
		} else {

			if (playList == null) {
				throw new NothingToPlayException();
			}

			// Loads the first file in list
			String file = playList.getFirstAudioFileName();
			if (file == null) {
				throw new NothingToPlayException();
			}
			playFile(file,false);

			// Then loads the second one
			String nextFileToPlay = playList.getNextAudioFileName();
			if (nextFileToPlay != null) {
				playFile(nextFileToPlay, true);
			}		

			state.set(State.PLAYING);
		}
	} // playAudioStream


	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private String normalizeFileName(String fileName) {
		return fileName.replace("\\", "/");
	}

	/**
	 * 
	 * @param file
	 * @param append
	 */
	private void playFile(String file,boolean append) {
		LOG.debug("Begin playFile");
		
		String tmpFile;
		try {
			tmpFile = copyFileToTmpDir(file);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		tmpFiles.add(tmpFile);

		String app = "";
		if (append) {
			app = " 1 ";
		}
		String command = "loadfile \""+normalizeFileName(tmpFile)+"\""+app+"\n";
		try {
			sendCommandToMPlayer(command);
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e);
			LOG.error("",ex);
			throw ex;
		}
	}



	/**
	 * 
	 */
	private void notifyNextStream() {

		LOG.debug("Begin notifyNextStream()");
		Thread t = new Thread(new Runnable() {

			public void run() {
				LOG.debug("Notify listeners");
				for (PlayerListener listener : listeners) {
					LOG.debug("Notify listener {}",listener);
					listener.nextStreamNotified();
				}

				LOG.debug("delete oldest tmp file");
				deleteOldestTmpFile();

				String nextFileToPlay = playList.getNextAudioFileName();
				if (nextFileToPlay != null) {
					playFile(nextFileToPlay, true);
				}		
			}
		});

		t.setName("t_NOTIFY_NEXT_STREAM");
		t.start();

	}

	/**
	 * 
	 */
	private void deleteOldestTmpFile() {

		
		String file = tmpFiles.remove(0);
		LOG.debug("File {} will be deleted",file);
		if (file != null) {
			File f = new File(file);
			boolean deleted = f.delete();
			if (!deleted) {
				LOG.warn("File has not been deleted !");
			}
		}

	}

	/**
	 * 
	 */
	private void notifyEnd() {
		for (PlayerListener listener : listeners) {
			listener.endNotified();
		}
	}

	/**
	 * 
	 */
	public void setGain(float gain) {

		int intGain = (int) (gain * 100);
		try {
			sendCommandToMPlayer("volume "+intGain+" 1");
		} catch (IOException e) {
			throw new RuntimeException("Error while setting volume");
		}


	}

	public void close() {

		mPlayerStandardOutputBuffer = null;


		if (mplayerProcess != null) {
			mplayerProcess.destroy();	
		}
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	private String copyFileToTmpDir(String file) throws Exception {
		LOG.debug("Begin copyFileToTmpDir");
		
		if (file == null || file.trim().length() == 0) {
			Exception ex = new IllegalStateException("file must bo not null");
			LOG.error("",ex);
			throw ex;
		}
		
		final File src = new File(file);
		LOG.debug("Will copy file {}",src.getName());
		
		if (!src.exists()) {
			Exception ex = new Exception("File "+src+" not found");
			LOG.error("",ex);
			throw ex;
		}
		
		final File dst = new File(tmpDir+"/"+src.getName());
	
		Thread copyThread = new Thread( new Runnable() {
	
			public void run() {
				InputStream is = null;
				OutputStream os = null;
				try {
					is = new FileInputStream(src);
					os = new FileOutputStream(dst);
					byte[] buffer = new byte[2048];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					} 
				} catch (IOException e) {
					RuntimeException ex = new RuntimeException(e);
					LOG.error("Error while copying {} to {}",src.getName(),dst.getName());
					LOG.error("",ex);
					throw ex;
				} finally {
					try {
						if (is != null) {
							is.close();
						}
						if (os != null) {
							os.flush();
							os.close();
						}
					} catch (IOException e) {
						// Ignore
					}
	
				}
			}
		});
	
		// Thread priority is set to min.
		copyThread.setPriority(Thread.MIN_PRIORITY);
		copyThread.start();
		while (!dst.exists()) {};
	
		return dst.getAbsolutePath();
	}



} // Play
