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

import com.github.biconou.AudioPlayer.api.PlayList;
import com.github.biconou.AudioPlayer.api.Player;
import junit.framework.Assert;
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

    private Player initPlayer() {
       return new JavaPlayer("MID [plughw:0,0]");
    }



    @Test
    public void playMultipleFiles() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Céline Frisch- Café Zimmermann - Bach- Goldberg Variations, Canons [Disc 1]/01 - Bach- Goldberg Variations, BWV 988 - Aria.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 10 Reed Neck Reel.mp3");



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

    /**
     * This test case verifies that changing the play list actually stops the player.
     *
     *
     * @throws Exception
     */
    @Test
    public void changePlayListWhilePlaying() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList1 = new ArrayListPlayList();
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");

        ArrayListPlayList playList2 = new ArrayListPlayList();
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");


        player.setPlayList(playList1);
        player.play();

        Thread.sleep(2000);

        player.setPlayList(playList2);
        System.out.println("+++ start play second play list +++");
        player.play();

        while (!player.isPlaying());

        while (player.isPlaying());
    }

    /**
     * This test case verifies that changing the play list actually stops the player.
     *
     *
     * @throws Exception
     */
    @Test
    public void changePlayListWhilePausing() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList1 = new ArrayListPlayList();
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList1.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");

        ArrayListPlayList playList2 = new ArrayListPlayList();
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");
        playList2.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");


        player.setPlayList(playList1);
        player.play();

        Thread.sleep(2000);
        player.pause();

        player.setPlayList(playList2);
        System.out.println("+++ start play second play list +++");
        player.play();

        while (!player.isPlaying());

        while (player.isPlaying());
    }

    @Test
    public void playDifferentFormats() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");


        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playStopPlay() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList arrayListPlayList = new ArrayListPlayList();
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 10 Reed Neck Reel.mp3");
        PlayList playList = arrayListPlayList;

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
    public void playClosePlay() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList arrayListPlayList = new ArrayListPlayList();
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");
        arrayListPlayList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 10 Reed Neck Reel.mp3");
        PlayList playList = arrayListPlayList;

        player.setPlayList(playList);
        player.registerListener(new ConsoleLogPlayerListener());
        Assert.assertEquals(Player.State.CLOSED,player.getState());
        player.setGain(0.9f);
        player.play();
        Thread.sleep(10000);
        Assert.assertEquals(Player.State.PLAYING,player.getState());
        player.close();
        player.play();
        Thread.sleep(5000);
        while (!Player.State.CLOSED.equals(player.getState()));
    }

    @Test
    public void playPlay() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.play();
        Thread.sleep(1000);
        try {
            player.play();
        } catch (AlreadyPlayingException ex) {
            // normal situation
        }
        while (!Player.State.CLOSED.equals(player.getState()));
    }

    @Test
    public void playGapLess() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac");
        playList.addAudioFile(resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());

    }

    /**
     * Play a single normal definition wav file.
     *
     * @throws Exception
     */
    @Test
    public void playWav() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(0.7f);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playFlac() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Céline Frisch- Café Zimmermann - Bach- Goldberg Variations, Canons [Disc 1]/01 - Bach- Goldberg Variations, BWV 988 - Aria.flac");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playMP3() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playOGG() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/OGG/01 - Sonata Violin & Cello I. Allegro.ogg");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playM4A() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/M4A/example.m4a");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playWavHD() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/WAV/naim-test-2-wav-24-96000.wav");

        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void playFlacHD() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Orfeo/01-01-Toccata-SMR.flac");


        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);
        player.play();
        while (!player.isPlaying());
        while (player.isPlaying());
    }

    @Test
    public void gain() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        playList.addAudioFile(resourcesBasePath()+"/Music/_DIR_ Sixteen Horsepower/_DIR_ Sackcloth 'n' Ashes/Sixteen Horsepower - 01 I Seen What I Saw.mp3");

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

        Player player = initPlayer();

        ArrayListPlayList playList = new ArrayListPlayList();
        //playList.addAudioFile(resourcesBasePath()+"/Music2/metallica/Fade to Black.mp3");
        playList.addAudioFile(resourcesBasePath()+"/count/count.mp3");


        player.registerListener(new ConsoleLogPlayerListener());
        player.setPlayList(playList);
        player.setGain(1);

        player.play();
        Thread.sleep(500);
        player.pause();
        Thread.sleep(3000);
        player.play();

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
