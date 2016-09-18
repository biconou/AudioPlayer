package com.github.biconou.AudioPlayer;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.junit.Test;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.FileInputStream;
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
    public void audioStream() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File file = null;
        AudioInputStream audioInputStream = null;

      /*  file = new File(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        audioInputStream = AudioSystem.getAudioInputStream(file);
        audioInputStream.getFormat().toString();
        */

        file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        //file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        audioInputStream = JavaPlayer.getAudioInputStream(file);
        //Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
        AudioFormat audioFormat = audioInputStream.getFormat();

        AudioFormat convertedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                (float)44100,
                16,
                audioFormat.getChannels(),
                4,
                (float)44100,
                false);

        AudioSystem.isConversionSupported(convertedFormat,audioFormat);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }

        // Create a SourceDataLine for play back (throws LineUnavailableException).
        SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
        // System.out.println("SourceDataLine class=" + dataLine.getClass());

        // The line acquires system resources (throws LineAvailableException).
        AudioInputStream toRead = audioInputStream;
        try {
            dataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            dataLine.open(convertedFormat);
            toRead = AudioSystem.getAudioInputStream(convertedFormat,audioInputStream);
            audioFormat = convertedFormat;
        }

        // Create a buffer for moving data from the audio stream to the line.
        final int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        int bytes = toRead.read(buffer, 0, bufferSize);
        while (bytes != -1) {
                dataLine.write(buffer, 0, bytes);
                bytes = toRead.read(buffer, 0, bufferSize);
            System.out.println(bytes);
        }

        while(1==1);

    }


    @Test
    public void play() throws Exception {

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
