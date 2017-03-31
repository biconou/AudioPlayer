package com.github.biconou.AudioPlayer;

import com.github.biconou.AudioPlayer.api.PlayList;
import com.github.biconou.AudioPlayer.api.Player;
import junit.framework.Assert;
import org.junit.Test;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TestJavaPlayer {

    private static String resourcesBasePath() {
        return TestJavaPlayer.class.getResource("/MEDIAS").getPath();
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


    @Test
    public void playSingle() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        File file = null;
        AudioInputStream audioInputStream = null;

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
        final int bufferSize = (int) audioFormat.getFrameRate() * audioFormat.getFrameSize();
        System.out.println(bufferSize);
        byte[] buffer = new byte[bufferSize];
        // Skip 5 seconds of music
        skip(5,audioInputStream,buffer,bufferSize);
        int bytes = audioInputStream.read(buffer, 0, bufferSize);
        while (bytes != -1) {
            dataLine.write(buffer, 0, bytes);
            bytes = audioInputStream.read(buffer, 0, bufferSize);
            System.out.println(bytes);
        }
    }

    private void skip(int seconds,AudioInputStream stream,byte[] buffer,int bytesPerSecond) throws IOException {
        for (int i=0;i<seconds;i++) {
            readOneSecond(stream,buffer,bytesPerSecond);
        }
    }

    private int readOneSecond(AudioInputStream stream,byte[] buffer,int bytesPerSecond) throws IOException {
        int bytes = 0;
        int totalBytesRead = 0;
        while (bytes != -1 && totalBytesRead < bytesPerSecond) {
            bytes = stream.read(buffer, totalBytesRead, bytesPerSecond - totalBytesRead);
            totalBytesRead += bytes;
        }
        return totalBytesRead;
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


        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();

        Thread.sleep(2000);
        player.setPos(1);

        Thread.sleep(4000);
        player.pause();
        Thread.sleep(4000);
        player.play();

        while (1==1);
    }

    @Test
    public void changePlayListWhilePlaying() throws Exception {

        ArrayListPlayList playList1 = new ArrayListPlayList();
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");

        ArrayListPlayList playList2 = new ArrayListPlayList();
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");


        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.setPlayList(playList1);
        player.play();

        Thread.sleep(2000);

        player.setPlayList(playList2);
        player.play();

        while (!player.isPlaying());

        while (player.isPlaying());
    }

    @Test
    public void playDifferentFormats() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");


        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playStopPlay() throws Exception {

        ArrayListPlayList arrayListPlayList = new ArrayListPlayList();
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 10 Reed Neck Reel.mp3");
        PlayList playList = arrayListPlayList;

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.setPlayList(playList);
        player.registerListener(new ConsoleLogPlayerListener());
        Assert.assertEquals(Player.State.CLOSED,player.getState());
        player.stop();
        Assert.assertEquals(Player.State.CLOSED,player.getState());
        player.play();
        Thread.sleep(1000);
        Assert.assertEquals(Player.State.PLAYING,player.getState());
        player.stop();
        Assert.assertEquals(Player.State.STOPPED,player.getState());
        player.stop();
        Assert.assertEquals(Player.State.STOPPED,player.getState());
        player.play();
        while (!player.isPlaying());
        while (playList.getIndex() == 0) {
            Thread.sleep(10);
        }
        player.stop();
        player.play();
        while (!player.isPlaying());
        Assert.assertEquals(1,playList.getIndex());
        while (player.isPlaying());
    }

    @Test
    public void playPlay() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(1000);
        player.play();
        while (!Player.State.CLOSED.equals(player.getState()));
    }

    @Test
    public void playGapLess() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");

        //Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        Player player = new JavaPlayer("CUBE [plughw:2,0]");
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playFlac() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Céline Frisch- Café Zimmermann - Bach- Goldberg Variations, Canons [Disc 1]/01 - Bach- Goldberg Variations, BWV 988 - Aria.flac");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playMP3() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playOGG() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/OGG/01 - Sonata Violin & Cello I. Allegro.ogg");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playHD() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");

        //Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        Player player = new JavaPlayer("CUBE [plughw:2,0]");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
    }

    @Test
    public void playFlacHD() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Orfeo/01-01-Toccata-SMR.flac");

        //Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        Player player = new JavaPlayer("CUBE [plughw:2,0]");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        while(1==1)
        Thread.sleep(30000);
    }

    @Test
    public void gain() throws Exception {
        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(1000);
        player.setGain(0);
        Thread.sleep(1000);
        player.setGain(0.3f);
        Thread.sleep(1000);
        player.setGain(0.5f);
        Thread.sleep(1000);
        player.setGain(0.8f);
        Thread.sleep(1000);
        player.setGain(1f);
        while (!Player.State.CLOSED.equals(player.getState())) {
            Thread.sleep(10);
        };
    }

    @Test
    public void movePosition() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        //playList.addAudioFile(resourcesBasePath()+"/Music2/metallica/Fade to Black.mp3");
        playList.addAudioFile(resourcesBasePath()+"/count/count.mp3");

        Player player = new JavaPlayer();

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);

        player.play();
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(1);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(2);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(3);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(4);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(5);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(6);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(7);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(8);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();

        player.setPos(9);
        Thread.sleep(500);
        player.pause();Thread.sleep(3000);player.play();



        while(1==1) {
            Thread.sleep(30000);
        }
    }

}
