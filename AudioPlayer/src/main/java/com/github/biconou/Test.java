package com.github.biconou;

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

import com.github.biconou.AudioPlayer.ArrayListPlayList;
import com.github.biconou.AudioPlayer.AudioSystemUtils;
import com.github.biconou.AudioPlayer.JavaPlayer;
import com.github.biconou.AudioPlayer.api.PlayList;
import com.github.biconou.AudioPlayer.api.Player;
import com.github.biconou.AudioPlayer.api.PlayerListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.util.Arrays;

public class Test {

    private static Logger log = LoggerFactory.getLogger(Test.class);

    private static String resourcesBasePath() {
        return Test.class.getResource("/samples").getPath();
    }

    private static void addToList(String audioFileName,String extension, ArrayListPlayList playList) throws IOException {
        InputStream in = Test.class.getResourceAsStream(audioFileName);
        File tmpFile = File.createTempFile("aud",extension);
        OutputStream out = new FileOutputStream(tmpFile);
        IOUtils.copy(in,out);
        in.close();
        out.close();

        playList.addAudioFile(tmpFile);
    }

    /**
     * Main méthod.
     *
     * Plays some audio samples.
     *
     * @param args
     */
    public static void main(String[] args) {

        ArrayListPlayList playList = new ArrayListPlayList();
        try {

            log.info("List of all available audion mixers");
            Arrays.stream(AudioSystemUtils.listAllMixers()).forEach(info -> {
                log.info("{} - {}",info.getName(),info.getDescription());
            });


            addToList("/samples/moog_16_44100.wav",".wav",playList);
            addToList("/samples/moog_16_44100.flac",".flac",playList);
            addToList("/samples/moog_16_44100_192.mp3",".mp3",playList);

            Player player = new JavaPlayer();
            player.setPlayList(playList);
            player.registerListener(new PlayerListener() {
                @Override
                public void onBegin(int index, File currentFile) {
                    System.out.println("BEGIN "+currentFile.getAbsolutePath());
                }

                @Override
                public void onEnd(int index, File currentFile) {
                    System.out.println("END "+currentFile.getAbsolutePath());
                }

                @Override
                public void onFinished() {
                    System.out.println("PLAYLIST FINISHED");
                }

                @Override
                public void onStop() {
                    System.out.println("STOPPING PLAYER");
                }

                @Override
                public void onPause() {

                }
            });
            player.play();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
