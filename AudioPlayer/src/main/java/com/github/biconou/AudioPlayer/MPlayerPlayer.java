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


	public MPlayerPlayer() throws IOException {
		mPlayerCommand = System.getProperty("com.github.biconou.AudioPlayer.MPlayerPlayer.command");
		
		tmpDir = System.getProperty("com.github.biconou.AudioPlayer.MPlayerPlayer.tmpDir");

		init();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public MPlayerPlayer(String mPlayerCommandLine) throws IOException {

		mPlayerCommand = mPlayerCommandLine;

		init();

	}

	/**
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {

		if (mPlayerCommand == null) {
			throw new IllegalStateException("Undefined MPlayer command.");
		}
		
		if (tmpDir == null) {
			throw new IllegalStateException("Undefined tmpDir.");
		}
		File tmpDirFile = new File(tmpDir);
		if (!tmpDirFile.exists() || !tmpDirFile.isDirectory()) {
			throw new IllegalStateException("tmpDir "+tmpDir+" does not exists or is not a directory");
		}

		ProcessBuilder pb = new ProcessBuilder(mPlayerCommand,"-slave","-idle","--gapless-audio","-v");
		mplayerProcess = pb.start();
		OutputStream MplayerOStream = mplayerProcess.getOutputStream();
		cmdStream = new BufferedOutputStream(MplayerOStream);
		

		final  BufferedReader MplayerIStream = new BufferedReader(new InputStreamReader(mplayerProcess.getInputStream()));
		final  BufferedReader MplayerErrorStream = new BufferedReader(new InputStreamReader(mplayerProcess.getErrorStream()));



		// Read MPlayer standard output
		Thread tOutputMplayer = new Thread(new Runnable() {

			public void run() {
				String readLine;
				try {
					readLine = MplayerIStream.readLine();
					while (readLine != null) {	            	  
						//System.out.println(readLine);
						if (readLine.startsWith("EOF code: 1")) {
							notifyNextStream();
						}

						// read next line
						readLine = MplayerIStream.readLine();
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		tOutputMplayer.start();


		// Read MPlayer standard error
		Thread tErrorMplayer = new Thread(new Runnable() {

			public void run() {
				String readLine;
				try {
					readLine = MplayerErrorStream.readLine();
					while (readLine != null) {	            	  
						System.out.println(readLine);

						// read next line
						readLine = MplayerErrorStream.readLine();
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		tErrorMplayer.start();

	}

	/** 
	 * Plays audio from given file names.
	 * @param args Command line parameters
	 */
	public static void main(String [] args) {
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
		if (!command.endsWith("\n")) {
			command = command + "\n";
		}
		System.out.println(command);
		cmdStream.write(command.getBytes("UTF-8"));
		cmdStream.flush();
	}


	/** 
	 * Play audio from the given file name. 
	 * @param fileName  The file to play
	 * @throws IOException 
	 */
	public static void playAudioFiles(String[] fileName) throws IOException {

		PlayList playList = new ArrayPlayList(fileName); 

		//Player player = new MPlayerPlayer("D:\\AudioPlayer\\mplayer2-x86_64-latest\\mplayer2.exe");
		Player player = new MPlayerPlayer();
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
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.github.biconou.AudioPlayer.Player#pause()
	 */
	public void pause() {
		try {
			sendCommandToMPlayer("pause");
		} catch (IOException e) {
			throw new RuntimeException(e);
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
				throw new RuntimeException(e);
			}
		} else {

			if (playList == null) {
				throw new NothingToPlayException();
			}

			String file = playList.getFirstAudioFileName();
			if (file == null) {
				throw new NothingToPlayException();
			}

			playFile(file,false);

			String nextFileToPlay = playList.getNextAudioFileName();
			if (nextFileToPlay != null) {
				playFile(nextFileToPlay, true);
			}		

			state.set(State.PLAYING);
		}
	} // playAudioStream


	/**
	 * 
	 * @param nextFileToPlay
	 * @return
	 * @throws IOException 
	 */
	private String copyFileToTmpDir(String nextFileToPlay) {

		final File src = new File(nextFileToPlay);
		final File dst = new File(tmpDir+"/"+src.getName());

		Thread copyThread = new Thread( new Runnable() {

			public void run() {
				InputStream is = null;
				OutputStream os = null;
				try {
					is = new FileInputStream(src);
					os = new FileOutputStream(dst);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					} 
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					try {
						is.close();
						os.close();
					} catch (IOException e) {
						// Ignore
					}
					
				}
			}
		});

		copyThread.start();

		while (!dst.exists()) {};
		
		return dst.getAbsolutePath();
	}

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
		
		String tmpFile = copyFileToTmpDir(file);
		tmpFiles.add(tmpFile);

		String app = "";
		if (append) {
			app = " 1 ";
		}
		String command = "loadfile \""+normalizeFileName(tmpFile)+"\""+app+"\n";
		try {
			sendCommandToMPlayer(command);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	/**
	 * 
	 */
	private void notifyNextStream() {
	
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				for (PlayerListener listener : listeners) {
					listener.nextStreamNotified();
				}
				
				deleteOldestTmpFile();
				
				String nextFileToPlay = playList.getNextAudioFileName();
				if (nextFileToPlay != null) {
					playFile(nextFileToPlay, true);
				}		
			}
		});
		
		t.start();
		
	}

	/**
	 * 
	 */
	private void deleteOldestTmpFile() {
		
		String file = tmpFiles.remove(0);
		if (file != null) {
			File f = new File(file);
			f.delete();
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
		if (mplayerProcess != null) {
			mplayerProcess.destroy();	
		}
	}



} // Play
