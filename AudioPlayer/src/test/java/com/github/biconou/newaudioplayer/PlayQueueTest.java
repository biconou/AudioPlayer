package com.github.biconou.newaudioplayer;

/*-
 * #%L
 * newaudioplayer
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

import com.github.biconou.newaudioplayer.impl.PlayQueue;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayQueueTest {


    private static String mixerName = "default [default]@localhost";

    /**
     * Play a single normal definition wav file.
     *
     * @throws Exception
     */
    @Test
    public void playSingleWav_16_44100() throws Exception {
        PlayQueue queue = new PlayQueue(mixerName);
        Path audioFilePath = Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        queue.add(audioFilePath);
        System.out.println("Start play");
        queue.play();
        Thread.sleep(10000);
    }

    @Test
    public void playPausePlay() throws Exception {
        PlayQueue queue = new PlayQueue(mixerName);
        Path audioFilePath = Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        queue.add(audioFilePath);
        System.out.println("play");
        queue.play();
        Thread.sleep(3000);
        System.out.println("Pause");
        queue.pause();
        Thread.sleep(3000);
        System.out.println("Play");
        queue.play();
        Thread.sleep(10000);
    }

    @Test
    public void playStopPlay() throws Exception {
        PlayQueue queue = new PlayQueue(mixerName);
        Path audioFilePath = Paths.get(TestResourcesUtils.resourcesBasePath()+"/WAV/naim-test-2-wav-16-44100.wav");
        queue.add(audioFilePath);
        System.out.println("play");
        queue.play();
        Thread.sleep(3000);
        System.out.println("stop");
        queue.stop();
        Thread.sleep(3000);
        System.out.println("Play");
        queue.play();
        Thread.sleep(10000);
    }

}
