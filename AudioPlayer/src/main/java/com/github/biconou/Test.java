package com.github.biconou;

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
