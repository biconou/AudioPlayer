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
public class SndPlayer {
	
	
    //ms.playSound("Z:/Ma musique/robert de vis�e - 1995 - lute, guitar & theorbo (toyohiko satoh)/22 - Passaquaille.flac");
	//ms.playSound("Z:/Ma musique/Henry Dumont - Motets � la chapelle de Louis XIV - FNAC MUSIC 592054/03 - Les Pages de la Chapelle-Muasica Aeterna-Olivier Schneebeli - Benedic Anima mea.wav");
	//ms.playSound("D:/subsonic/TestAudio/wav/t1.wav");
	//ms.playSound("D:/subsonic/TestAudio/wav/t2.wav");

    
    /** 
     * Plays audio from given file names.
     * @param args Command line parameters
     */
    public static void main(String [] args) {
    	
    	String[] filesToPlay = new String[3];
    	filesToPlay[0] = "D:/subsonic/TestAudio/wav/t1.wav";
    	filesToPlay[1] = "Z:/Ma musique/AC-DC/Back in Black/07 You Shook Me All Night Long.mp3";
    	filesToPlay[2] = "Z:/Ma musique/3 . Musique classique/3.9 classement par p�riodes/3.94 Epoque baroque/Rameau/Rameau - Hippolyte Et Aricie - Marc Minkowski - Les Musiciens Du Louvre/096 - Act 5  Vol Des Z�phirs.flac";
    	
    	
        // Process arguments.
        playAudioFile(filesToPlay);
        
        // Must exit explicitly since audio creates non-daemon threads.
        System.exit(0);
    } // main
    
    /** 
     * Play audio from the given file name. 
     * @param fileName  The file to play
     */
    public static void playAudioFile(String[] fileName) {
    	
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
    	
        playAudioStream(streams);
    } // playAudioFile
    
    /** 
     * Plays audio from the given audio input stream. 
     * @param audioInputStream  The audio stream to play
     */
    public static void playAudioStream(AudioInputStream[] audioInputStream) {
    	
    	AudioInputStream firstStream = audioInputStream[0];
        // Audio format provides information like sample rate, size, channels.
        AudioFormat audioFormat = firstStream.getFormat();
        System.out.println("Play input audio format=" + audioFormat);
        
        AudioFormat newFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, 
                audioFormat.getSampleRate(),
                16,
                audioFormat.getChannels(),
                audioFormat.getChannels() * 2,
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
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
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
            int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
            
            // CODE MODIFIE
            try {
				BufferedAudioInputStream bufferedStream = new BufferedAudioInputStream(audioInputStream,bufferSize);
				
		        int nBytesRead = 0;
		        byte[] abData = new byte[bufferSize];

		        while (nBytesRead >= 0) {
		            try {
		            	nBytesRead = bufferedStream.read(abData);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		            if (nBytesRead > 0) {
		                @SuppressWarnings("unused")
		                int nBytesWritten = dataLine.write(abData, 0, nBytesRead);
		            }
		        }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
            /*
            byte [] buffer = new byte[ bufferSize ];
            
            // Move the data until done or there is an error.
            try {
                int bytesRead = 0;
                while (bytesRead >= 0) {
                    bytesRead = audioInputStream.read(buffer, 0, buffer.length);
                    if (bytesRead >= 0) {
                        // System.out.println("Play.playAudioStream bytes read=" + bytesRead +
                        //    ", frame size=" + audioFormat.getFrameSize() + ", frames read=" + bytesRead / audioFormat.getFrameSize());
                        // Odd sized sounds throw an exception if we don't write the same amount.
                        dataLine.write(buffer, 0, bytesRead);
                    }
                } // while
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            
            System.out.println("Play.playAudioStream draining line.");
            // Continues data line I/O until its buffer is drained.
            dataLine.drain();
            
            System.out.println("Play.playAudioStream closing line.");
            // Closes the data line, freeing any resources such as the audio device.
            dataLine.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    } // playAudioStream
} // Play
