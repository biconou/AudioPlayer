package com.github.biconou.AudioPlayer;

import org.junit.Test;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class TestAudioPlayer {

    private static String resourcesBasePath() {
        return TestAudioPlayer.class.getResource("/MEDIAS").getPath();
    }


    @Test
    public void mixer() {
        AudioSystem.getMixerInfo();
        AudioSystem.getAudioFileTypes();
    }

    @Test
    public void ckeckDataLines() {

        JavaPlayer.supportedFormats.forEach((s, audioFormat) -> System.out.println(s));
    }


    @Test
    public void playSingleWav() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        File file = null;
        AudioInputStream audioInputStream = null;

        //file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        audioInputStream = JavaPlayer.getAudioInputStream(file);
        AudioFormat audioFormat = audioInputStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }
        SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);

        // The line acquires system resources (throws LineAvailableException).
        try {
            dataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        dataLine.start();

        // Buffer contains 1 second of music.
        final int bufferSize = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        // Skip 5 seconds of music
        audioInputStream.skip(5 * bufferSize);
        int bytes = audioInputStream.read(buffer, 0, bufferSize);
        while (bytes != -1) {
                dataLine.write(buffer, 0, bytes);
                bytes = audioInputStream.read(buffer, 0, bufferSize);
            System.out.println(bytes);
        }
    }


    @Test
    public void playMultipleFiles() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Céline Frisch- Café Zimmermann - Bach- Goldberg Variations, Canons [Disc 1]/01 - Bach- Goldberg Variations, BWV 988 - Aria.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 10 Reed Neck Reel.mp3");


        Player player = new JavaPlayer();
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();

        while (1==1);

    }
}
