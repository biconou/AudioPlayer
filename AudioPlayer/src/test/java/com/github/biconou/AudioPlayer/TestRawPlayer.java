package com.github.biconou.AudioPlayer;

import org.junit.Test;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by remi on 05/04/17.
 */
public class TestRawPlayer {

    private static String resourcesBasePath() {
        return TestJavaPlayer.class.getResource("/MEDIAS").getPath();
    }


    @Test
    public void playSingle() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        File file;
        AudioInputStream audioInputStream;

        file = new File(resourcesBasePath()+"/count/count.mp3");
        JavaPlayer javaPlayer = new JavaPlayer();
        audioInputStream = javaPlayer.getAudioInputStream(file);
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
        System.out.println("Frame rate : "+audioFormat.getFrameRate());
        System.out.println("Frame size : "+audioFormat.getFrameSize());
        final int bytesPerSecond = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        System.out.println("bytesPerSecond" + bytesPerSecond);
        // Skip 5 seconds of music
        //skip(5,audioInputStream,buffer,bufferSize);

        AudioBuffers audioBuffers = new AudioBuffers(audioInputStream);
        audioBuffers.fillBuffers();

        byte[] buffer = audioBuffers.getOneSecondOfMusic();
        while ( buffer != null ) {
            dataLine.write(buffer, 0, bytesPerSecond);
            buffer = audioBuffers.getOneSecondOfMusic();
        }
    }

    private void skip(int seconds,AudioInputStream stream,byte[] buffer,int bytesPerSecond) throws IOException {
        for (int i=0;i<seconds;i++) {
            AudioSystemUtils.readOneSecond(stream,buffer,bytesPerSecond);
        }
    }

}
