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

import com.github.biconou.AudioPlayer.api.PlayQueue;
import com.github.biconou.AudioPlayer.impl.CamelAudioPlayer;
import com.github.biconou.AudioPlayer.impl.PlayQueueImpl;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayQueueTest {


    private static String mixerName = "MID [plughw:0,0]";

    /**
     * Play a single normal definition wav file.
     *
     * @throws Exception
     */
    @Test
    public void playWav() throws Exception {
        PlayQueue queue = PlayQueueImpl.Companion.createPlayQueue(mixerName);
        Path audioFilePath = Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        queue.add(audioFilePath);
        queue.play();
        Thread.sleep(10000);
    }

    @Test
    public void playPausePlay() throws Exception {
        PlayQueue queue = PlayQueueImpl.Companion.createPlayQueue(mixerName);
        Path audioFilePath = Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        queue.add(audioFilePath);
        queue.play();
        Thread.sleep(3000);
        queue.pause();
        Thread.sleep(3000);
        queue.play();
        Thread.sleep(20000);
    }

    @Test
    public void playMultiple() throws Exception {
        PlayQueue queue = PlayQueueImpl.Companion.createPlayQueue(mixerName);
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav"));
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/count/count.wav"));
        queue.play();
        Thread.sleep(50000);
    }

    @Test
    public void playStopPlay() throws Exception {
        PlayQueue queue = PlayQueueImpl.Companion.createPlayQueue(mixerName);
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav"));
        queue.play();
        Thread.sleep(3000);
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/count/count.wav"));
        Thread.sleep(12000);
        queue.stop();

        Thread.sleep(50000);
    }

    @Test
    public void playGapLess() throws Exception {
        PlayQueue queue = PlayQueueImpl.Companion.createPlayQueue(mixerName);
        System.out.println("+++++ wait 10 seconds before to add audio files");
        Thread.sleep(10000);
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 1.flac"));
        queue.add(Paths.get(TestResourcesUtils.resourcesBasePath()+"/Music2/Heiner Goebbels Surrogate Cities/01 Surrogate Cities part 1 - 2.flac"));
        System.out.println("+++++ wait 5 seconds before start playing");
        Thread.sleep(5000);
        queue.play();
        Thread.sleep(50000);
    }
}
