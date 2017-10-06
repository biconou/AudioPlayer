package com.github.biconou.AudioPlayer;

/*-
 * #%L
 * AudioPlayer
 * %%
 * Copyright (C) 2016 - 2017 Rémi Cocula
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.sun.media.sound.JDK13Services;
import org.junit.Test;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * Created by remi on 05/04/17.
 */
public class TestRawPlayer {

    private static String resourcesBasePath() {
        return TestJavaPlayer.class.getResource("/MEDIAS").getPath();
    }


    @Test
    public void listAllMixers() {
        Arrays.stream(AudioSystem.getMixerInfo()).forEach(info -> {
            System.out.println(info.getName() + " : " + info.getDescription());
        });
    }

    @Test
    public void toto() throws LineUnavailableException {
        Mixer mixer = AudioSystemUtils.findMixerByName("Port CUBE [hw:1]");
        mixer.open();
        mixer.getControls();
    }

    public void listLinesForMixer(String mixerName) {
        Mixer mixer = AudioSystemUtils.findMixerByName(mixerName);
        Line.Info[] lineInfos = mixer.getSourceLineInfo();
        Arrays.stream(lineInfos).forEach(lineInfo -> {
            System.out.println("       " + lineInfo.getLineClass().getName() + " " + lineInfo.toString());
            if (lineInfo instanceof DataLine.Info) {
                AudioFormat[] allFormats = ((DataLine.Info) lineInfo).getFormats();
                Arrays.stream(allFormats).forEach(f -> System.out.println("              " + f.toString()));
            }
        });
    }

    @Test
    public void ListLinesForCUBE() {
        listLinesForMixer("CUBE [plughw:1,0]");
    }

    @Test
    public void ListLinesForMID() {
        listLinesForMixer("MID [plughw:0,0]");
    }


    @Test
    public void AudioReaders() {
        JDK13Services.getProviders(AudioFileReader.class).stream().forEach(System.out::println);
    }


    private File resolveFile(String fileName) {
        return new File(resourcesBasePath() + fileName);
    }

    @Test
    public void playAll() throws Exception {
        String mixerName = "MID [plughw:0,0]";
        //       String mixerName = "CUBE [plughw:1,0]";
        //String mixerName = "default [default]";
        playSingle(mixerName, "/count/count.wav");
//        playSingle(mixerName, "/count/count.mp3");
//        playSingle(mixerName, "/count/count.ogg");
//        playSingle(mixerName, "/count/count.flac");
//        playSingle(mixerName, "/Orfeo/01-01-Toccata-SMR.flac");
//        playSingle(mixerName, "/WAV/naim-test-2-wav-16-44100.wav");
//        playSingle(mixerName, "/WAV/naim-test-2-wav-24-96000.wav");
//        playSingle(mixerName, "/OGG/01 - Sonata Violin & Cello I. Allegro.ogg");
//        playSingle(mixerName, "/OGG/02 - Sonata Violin & Cello II. Tres Vif.ogg");
//        playSingle(mixerName, "/Music2/_DIR_ chrome hoof - 2004/02 eyes like dull hazlenuts.mp3");
//        playSingle(mixerName, "/Music2/_DIR_ chrome hoof - 2004/10 telegraph hill.mp3");
//        playSingle(mixerName, "/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
//        playSingle(mixerName, "/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
//        playSingle(mixerName, "/Music2/metallica/Fade to Black.flac");
//        playSingle(mixerName, "/Music2/metallica/Fade to Black.mp3");
    }

    @Test
    public void playAllUsingClip() throws Exception {
        String mixerName = "CUBE [plughw:1,0]";
        //String mixerName = "default [default]";
//        playSingle(mixerName,"/count/count.wav");
//        playSingle(mixerName,"/count/count.mp3");
//        playSingle(mixerName,"/count/count.ogg");
//        playSingle(mixerName,"/count/count.flac");
        //      playSingle(mixerName,"/Orfeo/01-01-Toccata-SMR.flac");
        playSingleUsingClip(mixerName, "/WAV/naim-test-2-wav-16-44100.wav");
//         playSingle(mixerName,"/WAV/naim-test-2-wav-24-96000.wav");
//        playSingle(mixerName,"/OGG/01 - Sonata Violin & Cello I. Allegro.ogg");
//        playSingle(mixerName,"/OGG/02 - Sonata Violin & Cello II. Tres Vif.ogg");
//        playSingle(mixerName,"/Music2/_DIR_ chrome hoof - 2004/02 eyes like dull hazlenuts.mp3");
//        playSingle(mixerName,"/Music2/_DIR_ chrome hoof - 2004/10 telegraph hill.mp3");
//        playSingle(mixerName,"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
//        playSingle(mixerName,"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
//        playSingle(mixerName,"/Music2/metallica/Fade to Black.flac");
//        playSingle(mixerName,"/Music2/metallica/Fade to Black.mp3");*/
    }


    private void applyGain(SourceDataLine dataLine, float gain) {
        if (gain != -1) {
            if ((gain < 0) || (gain > 1)) {
                throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
            }
        }

        if (dataLine == null) {
            return;
        }

        if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
            if (gain == -1) {
                control.setValue(0);
            } else {
                float max = control.getMaximum();
                float min = control.getMinimum(); // negative values all seem to be zero?
                float range = max - min;

                control.setValue(min + (range * gain));
            }
        } else {
            System.out.println("WARNING : MASTER_GAIN not supported");
        }
    }


    private AudioInputStream prepareAudioInputStream(String fileName) throws Exception {
        File file = resolveFile(fileName);
        AudioInputStream audioInputStream = AudioInputStreamUtils.getAudioInputStream(file);
        return audioInputStream;
    }

    private SourceDataLine prepareSourceDataLine(String mixerName, AudioInputStream audioInputStream) throws LineUnavailableException {
        Mixer mixer = AudioSystemUtils.findMixerByName(mixerName);
        AudioFormat audioFormat = audioInputStream.getFormat();

        System.out.println(audioFormat);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine dataLine = (SourceDataLine) mixer.getLine(info);

        dataLine.open(audioFormat);

        dataLine.start();
        applyGain(dataLine, (float) 0.8);

        return dataLine;
    }

    public void playUsingSimpleLoop(AudioInputStream audioInputStream, SourceDataLine dataLine) throws Exception {
        AudioFormat audioFormat = audioInputStream.getFormat();
        // Buffer contains 1 second of music.
        System.out.println("Frame rate : " + audioFormat.getFrameRate());
        System.out.println("Frame size : " + audioFormat.getFrameSize());
        int bytesPerSecond = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        System.out.println("bytesPerSecond : " + bytesPerSecond);
        byte[] buffer = new byte[bytesPerSecond];
        int byteRead = AudioSystemUtils.readOneSecond(audioInputStream, buffer, bytesPerSecond);
        while (byteRead > 0) {
            System.out.println(Thread.currentThread().getName() + "write one buffer to line");
            dataLine.write(buffer, 0, byteRead);
            byteRead = AudioSystemUtils.readOneSecond(audioInputStream, buffer, bytesPerSecond);
        }
    }

    public void playUsingBlockingQueue(AudioInputStream audioInputStream, SourceDataLine dataLine) throws Exception {
        AudioFormat audioFormat = audioInputStream.getFormat();
        // Buffer contains 1 second of music.
        System.out.println("Frame rate : " + audioFormat.getFrameRate());
        System.out.println("Frame size : " + audioFormat.getFrameSize());
        int bytesPerSecond = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        System.out.println("bytesPerSecond : " + bytesPerSecond);

        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>(4);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                while (1==1) {
                    byte[] buffer = null;
                    buffer = queue.take();
                    System.out.println(Thread.currentThread().getName() + "write one buffer to line");
                    dataLine.write(buffer, 0, buffer.length);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        byte[] buffer = new byte[bytesPerSecond];
        int byteRead = AudioSystemUtils.readOneSecond(audioInputStream, buffer, bytesPerSecond);
        while (byteRead > 0) {
            queue.put(buffer);
            System.out.println(Thread.currentThread().getName() + " : " + byteRead + " bytes read, queue size = " + queue.size());
            buffer = new byte[bytesPerSecond];
            byteRead = AudioSystemUtils.readOneSecond(audioInputStream, buffer, bytesPerSecond);
        }

        executor.shutdown();
    }

    /**
     * @param mixerName
     * @param fileName
     * @throws Exception
     */
    private void playSingle(String mixerName, String fileName) throws Exception {

        AudioInputStream audioInputStream = prepareAudioInputStream(fileName);
        SourceDataLine dataLine = prepareSourceDataLine(mixerName,audioInputStream);

        playUsingSimpleLoop(audioInputStream,dataLine);

        dataLine.drain();
        dataLine.close();
    }

    /**
     * @param mixerName
     * @param fileName
     * @throws Exception
     */
    @Test
    public void playSingleUsingLoop() throws Exception {

        String mixerName = "MID [plughw:0,0]";

        AudioInputStream audioInputStream = prepareAudioInputStream("/count/count.wav");
        SourceDataLine dataLine = prepareSourceDataLine(mixerName,audioInputStream);

        playUsingSimpleLoop(audioInputStream,dataLine);

        dataLine.drain();
        dataLine.close();
    }

    /**
     * @param mixerName
     * @param fileName
     * @throws Exception
     */
    @Test
    public void playSingleUsingBlockingQueue() throws Exception {

        String mixerName = "MID [plughw:0,0]";

        AudioInputStream audioInputStream = prepareAudioInputStream("/count/count.wav");
        SourceDataLine dataLine = prepareSourceDataLine(mixerName,audioInputStream);

        playUsingBlockingQueue(audioInputStream,dataLine);

        dataLine.drain();
        dataLine.close();
    }

    private void playSingleUsingClip(String mixerName, String fileName) throws Exception {

        System.out.println("+++PLAYING+++ = " + fileName);
        File file = resolveFile(fileName);
        AudioInputStream audioInputStream = AudioInputStreamUtils.getAudioInputStream(file);
        Mixer mixer = AudioSystemUtils.findMixerByName(mixerName);

        AudioFormat audioFormat = audioInputStream.getFormat();
        System.out.println(audioFormat);

        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }
        Clip dataLine = (Clip) mixer.getLine(info);

        try {
            dataLine.open(audioInputStream);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        System.out.println(dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN));
        dataLine.start();
        //applyGain(dataLine,(float)0.8);

        while (1 == 1) ;
    }

    @Test
    public void playTwoFilesUsingClip() throws Exception {


        String mixerName = "MID [plughw:0,0]";
        Mixer mixer = AudioSystemUtils.findMixerByName(mixerName);

        File file1 = resolveFile("/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        AudioInputStream audioInputStream1 = AudioInputStreamUtils.getAudioInputStream(file1);
        File file2 = resolveFile("/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
        AudioInputStream audioInputStream2 = AudioInputStreamUtils.getAudioInputStream(file2);


        AudioFormat audioFormat = audioInputStream1.getFormat();
        System.out.println(audioFormat);

        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }
        Clip dataLine = (Clip) mixer.getLine(info);

        dataLine.open(audioInputStream1);
        dataLine.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event)  {
                if (event.getType().equals(LineEvent.Type.STOP)) {
                    try {
                        dataLine.close();
                        dataLine.open(audioInputStream2);
                        dataLine.start();
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dataLine.start();

        while (1 == 1) ;
    }




    @Test
    public void playSingleUsingAudioBuffers() throws Exception {

        File file;
        AudioInputStream audioInputStream;

        file = new File(resourcesBasePath() + "/count/count.mp3");
        audioInputStream = AudioSystemUtils.getAudioInputStream(file);
        AudioFormat audioFormat = audioInputStream.getFormat();

        // Choose a mixer
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(infos[0]);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }
        SourceDataLine dataLine = (SourceDataLine) mixer.getLine(info);

        // The line acquires system resources (throws LineAvailableException).
        try {
            dataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        dataLine.start();

        // Buffer contains 1 second of music.
        System.out.println("Frame rate : " + audioFormat.getFrameRate());
        System.out.println("Frame size : " + audioFormat.getFrameSize());
        final int bytesPerSecond = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        System.out.println("bytesPerSecond" + bytesPerSecond);
        // Skip 5 seconds of music
        //skip(5,audioInputStream,buffer,bufferSize);

        AudioBuffers audioBuffers = new AudioBuffers(audioInputStream);
        audioBuffers.fillBuffers();

        AudioBuffers.BufferHolder holder = audioBuffers.getOneSecondOfMusic();
        while (holder != null) {
            dataLine.write(holder.buffer, 0, holder.bytes);
            holder = audioBuffers.getOneSecondOfMusic();
        }
    }

    private void skip(int seconds, AudioInputStream stream, byte[] buffer, int bytesPerSecond) throws IOException {
        for (int i = 0; i < seconds; i++) {
            AudioSystemUtils.readOneSecond(stream, buffer, bytesPerSecond);
        }
    }

}
