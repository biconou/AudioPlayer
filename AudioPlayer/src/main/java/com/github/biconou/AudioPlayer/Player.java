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



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
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
public class Player {
	
    public static enum State {
        PAUSED,
        PLAYING,
        CLOSED
    }

	
    
    /**
     * Returns the player state.
     */
    public State getState() {
        return state.get();
    }

    private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);
	
    //ms.playSound("Z:/Ma musique/robert de vis�e - 1995 - lute, guitar & theorbo (toyohiko satoh)/22 - Passaquaille.flac");
	//ms.playSound("Z:/Ma musique/Henry Dumont - Motets � la chapelle de Louis XIV - FNAC MUSIC 592054/03 - Les Pages de la Chapelle-Muasica Aeterna-Olivier Schneebeli - Benedic Anima mea.wav");
	//ms.playSound("D:/subsonic/TestAudio/wav/t1.wav");
	//ms.playSound("D:/subsonic/TestAudio/wav/t2.wav");

    
	PlayList playList = null;
	Thread audioStreamWriterThread = null;
	
	SourceDataLine dataLine = null;
	
	BufferedMultipleAudioInputStream bufferedStream = null;
	
	List<PlayerListener> listeners = new ArrayList<PlayerListener>();
	
	Boolean mustPause = Boolean.FALSE;
	Boolean mustStop = Boolean.FALSE;
	
	
    /** 
     * Plays audio from given file names.
     * @param args Command line parameters
     */
    public static void main(String [] args) {
    	
    	//String[] filesToPlay = new String[1];
    	//filesToPlay[0] = "D:/subsonic/TestAudio/wav/t1.wav";
    	//filesToPlay[0] = "D:/subsonic/TestAudio/wav/M1F1-int24-AFsp.wav";
    	//filesToPlay[1] = "D:/TEST_BASE_STREAMING/Music/Céline Frisch- Café Zimmermann - Bach- Goldberg Variations, Canons [Disc 1]/01 - Bach- Goldberg Variations, BWV 988 - Aria.flac";
    	//filesToPlay[1] = "Z:/Ma musique/AC-DC/Back in Black/07 You Shook Me All Night Long.mp3";
    	//filesToPlay[2] = "Z:/Ma musique/3 . Musique classique/3.9 classement par périodes/3.94 Epoque baroque/Balbastre/Balbastre - Pièces de clavecin - premier livre (1759) - Jean-Patrice Brosse/01 - La de Caze - Jean-Patrice Brosse.flac";
    	
    	
        // Process arguments.
        playAudioFiles(args);
    } // main
    
    
    /**
     * 
     * @param palyList
     */
    public void setPlayList(PlayList playList) {
    	this.playList = playList;
    }
    
    /**
     * 
     */
    public void registerListener(PlayerListener listener) {
    	listeners.add(listener);
    }
    
    
    /** 
     * Play audio from the given file name. 
     * @param fileName  The file to play
     */
    public static void playAudioFiles(String[] fileName) {
    	
    	AudioInputStream[] streams = new AudioInputStream[fileName.length];

    	for (int i = 0; i < fileName.length; i++) {
        	
    		File soundFile = new File(fileName[i]);
            
            try {
                // Create a stream from the given file.
                // Throws IOException or UnsupportedAudioFileException
            	
                streams[i] = AudioSystem.getAudioInputStream(soundFile);
                // AudioSystem.getAudioInputStream(inputStream); // alternate audio stream from inputstream
            } catch (Exception e) {
                System.out.println("Problem with file " + fileName + ":");
                e.printStackTrace();
            }            
		}
    	
    	PlayList playList = new AudioStreamArrayPlayList(streams); 
    	
    	Player player = new Player();
    	player.setPlayList(playList);
    	try {
			player.play();
		} catch (NothingToPlayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
    
    
    /**
     * 
     */
    public void stop() {
		mustPause = Boolean.TRUE;
		mustStop = Boolean.TRUE;
		if (bufferedStream != null) {
			bufferedStream.stopFillBuffers();
			bufferedStream = null;
		}
    	state.set(State.CLOSED);
		
    	stopLine();
    }
    
    /**
     * 
     */
    public void pause() {
    	mustPause = Boolean.TRUE;
    	state.set(State.PAUSED);
    }
    
    /** 
     * Plays audio from the given audio input stream. 
     * @param audioInputStream  The audio stream to play
     */
    public void play() throws NothingToPlayException {

    	if (getState().equals(State.PAUSED)) {
    		mustPause = Boolean.FALSE;
    	} else {

    		if (playList == null) {
    			throw new NothingToPlayException();
    		}

    		AudioInputStream firstStream = playList.getFirstAudioStream();
    		if (firstStream == null) {
    			throw new NothingToPlayException();
    		}

    		// Audio format provides information like sample rate, size, channels.
    		AudioFormat audioFormat = firstStream.getFormat();
    		System.out.println("Play input audio format=" + audioFormat);
    		System.out.println("Frame size : "+audioFormat.getFrameSize());
    		System.out.println("Frame rate : "+audioFormat.getFrameRate());

    		int targetSampleSizeInBits = audioFormat.getSampleSizeInBits();
    		if (targetSampleSizeInBits == -1) {
    			targetSampleSizeInBits = 16;
    		}
    		
    		int targetFrameSize = 4;
    		if (targetSampleSizeInBits == 24) {
    			targetFrameSize = 6;
    		}
    		
    		AudioFormat newFormat = new AudioFormat(
    				AudioFormat.Encoding.PCM_SIGNED, 
    				audioFormat.getSampleRate(),
    				targetSampleSizeInBits,
    				audioFormat.getChannels(),
    				targetFrameSize,
    				audioFormat.getSampleRate(),
    				false);


    		audioFormat = newFormat;


    		// Open a data line to play our type of sampled audio.
    		// Use SourceDataLine for play and TargetDataLine for record.
    		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
    		if (!AudioSystem.isLineSupported(info)) {
    			System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
    			return;
    		}

    		try {
    			// Create a SourceDataLine for play back (throws LineUnavailableException).  
    			dataLine = (SourceDataLine) AudioSystem.getLine(info);
    			// System.out.println("SourceDataLine class=" + dataLine.getClass());

    			// The line acquires system resources (throws LineAvailableException).
    			dataLine.open(audioFormat);

    			// Adjust the volume on the output line.
    			//            if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
    			//                FloatControl volume = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
    			//                volume.setValue(100.0F);
    			//            }

    			// Allows the line to move data in and out to a port.
    			dataLine.start();


    			// Create a buffer for moving data from the audio stream to the line.   
    			final int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();

    			// CODE MODIFIE
    			try {
    				bufferedStream = new BufferedMultipleAudioInputStream(playList,bufferSize);

    				audioStreamWriterThread = new Thread(new Runnable() {

    					public void run() {
    						int nBytesRead = 0;
    						byte[] abData = new byte[bufferSize];

    						while (nBytesRead >= 0) {
    							if (mustPause.equals(Boolean.FALSE)) {
    								try {
    									nBytesRead = bufferedStream.read(abData);
    								} catch (Exception e) {
    									e.printStackTrace();
    								}
    								if (nBytesRead > 0) {
    									@SuppressWarnings("unused")
    									int nBytesWritten = dataLine.write(abData, 0, nBytesRead);
    									//System.out.println("bits written to line : "+nBytesWritten);
    								} else {
    									if (nBytesRead == -1) {
    										stopLine();
    									} else if (nBytesRead == -2) {
    										notifyNextStream();
    										nBytesRead = 0;
    									}

    								}											        			
    							} else {
    								if (mustStop.equals(Boolean.TRUE)) {
    									break;
    								} else {
    									try {
    										Thread.sleep(500);
    									} catch (InterruptedException e) {
    										// TODO Auto-generated catch block
    										e.printStackTrace();
    									}
    								}
    							}
    						}
    					}
    				});

    				state.set(State.PLAYING);

    				audioStreamWriterThread.start();


    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}



    		} catch (LineUnavailableException e) {
    			e.printStackTrace();
    		}
    	}

    	state.set(State.PLAYING);

    } // playAudioStream
    
    

    /**
     * 
     */
    private void notifyNextStream() {
    	for (PlayerListener listener : listeners) {
			listener.nextStreamNotified();
		}
    }
    
    
    /**
     * 
     */
    private void stopLine() {
    	
    	if (dataLine != null) {
    		System.out.println("Play.playAudioStream draining line.");
    		// Continues data line I/O until its buffer is drained.
    		dataLine.drain();

    		System.out.println("Play.playAudioStream closing line.");
    		// Closes the data line, freeing any resources such as the audio device.
    		dataLine.close();    	
    	}
    }
} // Play
