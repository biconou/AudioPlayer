package com.github.biconou.AudioPlayer;

import org.junit.Test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import java.util.Arrays;

public class TestAudioPlayer {

    private static String resourcesBasePath() {
        return TestAudioPlayer.class.getResource("/MEDIAS").getPath();
    }


    @Test
    public void mixer() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Arrays.stream(infos).forEach(info -> {
            System.out.println(info.getName()+" : "+info.getDescription());
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getSourceLineInfo();
            Arrays.stream(lineInfos).forEach(lineInfo -> {
                //System.out.println(lineInfo.toString());
            });
        });
        AudioSystem.getAudioFileTypes();
    }


 /*   @Test
    public void playSingleWav() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        File file = null;
        AudioInputStream audioInputStream = null;

        //file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        file = new File(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        audioInputStream = JavaPlayer.getAudioInputStream(file);
        AudioFormat audioFormat = audioInputStream.getFormat();

        // Choose a mixer
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(infos[3]);

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
    } */


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


        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();

        Thread.sleep(4000);
        player.setPos(5);


        while (1==1);

    }
}
