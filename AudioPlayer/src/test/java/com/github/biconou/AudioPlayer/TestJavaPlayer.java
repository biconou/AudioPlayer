package com.github.biconou.AudioPlayer;

import com.github.biconou.AudioPlayer.api.Player;
import org.junit.Test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
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

        Thread.sleep(2000);
        player.setPos(1);

        Thread.sleep(4000);
        player.pause();
        Thread.sleep(4000);
        player.play();

        while (1==1);
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

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.stop();
        player.setPlayList(playList);
        player.play();
        Thread.sleep(5000);
        player.stop();
        player.stop();
        playList.reset();
        player.play();

        Thread.sleep(40000);
        player.stop();

        //while (1==1);
    }

    @Test
    public void playPlay() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        Player player = new JavaPlayer(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(30000);
        playList.reset();
        player.play();
        Thread.sleep(30000);
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
        Thread.sleep(2000);
        player.setGain(0);
        Thread.sleep(2000);
        player.setGain((float)0.3);
        Thread.sleep(2000);
        player.setGain((float)0.5);
        Thread.sleep(2000);
        player.setGain((float)0.8);
        Thread.sleep(2000);
        player.setGain((float)1);
        Thread.sleep(30000);
    }

    @Test
    public void movePosition() throws Exception {

        ArrayListPlayList playList = new ArrayListPlayList();
        //playList.addAudioFile(resourcesBasePath()+"/Music2/metallica/Fade to Black.mp3");
        playList.addAudioFile(resourcesBasePath()+"/count/count.wav");

        Player player = new JavaPlayer();

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(500);
        player.setPos(1);
        Thread.sleep(500);
        player.setPos(2);
        Thread.sleep(500);
        player.setPos(3);
        Thread.sleep(500);
        player.setPos(4);
        Thread.sleep(500);
        player.setPos(5);
        Thread.sleep(500);
        player.setPos(6);
        Thread.sleep(500);
        player.setPos(7);
        Thread.sleep(500);
        player.setPos(8);
        Thread.sleep(500);
        player.setPos(9);
        Thread.sleep(500);

       /* System.out.println("skip to 2 seconds");
        player.setPos(8);
        Thread.sleep(500);
*/
    /*    System.out.println("skip to 5 seconds");
        player.setPos(5);
        Thread.sleep(500);

        System.out.println("skip to 9 seconds");
        player.setPos(9);
        Thread.sleep(500);
        */

        while(1==1) {
            Thread.sleep(30000);
        }
    }

}
