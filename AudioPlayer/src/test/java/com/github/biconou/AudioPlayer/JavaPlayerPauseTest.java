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

public class JavaPlayerPauseTest {


    private Player initPlayer() {
        return new JavaPlayer("default [default]");
    }


    @Test
    public void playPause() throws Exception {

        Player player = initPlayer();

        ArrayListPlayList arrayListPlayList = new ArrayListPlayList();
        arrayListPlayList.addAudioFile(TestUtils.resourcesBasePath() + "/count/count.mp3");
        PlayList playList = arrayListPlayList;

        player.setPlayList(playList);
        player.registerListener(new ConsoleLogPlayerListener());
        Assert.assertEquals(Player.State.CLOSED,player.getState());
        player.setGain(0.9f);
        player.play();
        //Assert.assertEquals(Player.State.PLAYING,player.getState());

        Thread.sleep(5000);
        player.pause();
        //Assert.assertEquals(Player.State.PAUSED,player.getState());

        Thread.sleep(30000);

        player.play();

        while (1 == 1);
    }

    @Test
    public void testLoop() throws InterruptedException {

        boolean continu = true;

        while (continu) {
            if (continu == false) {
                continu = true;
                Thread.sleep(10000);
            }
        }
    }


}
