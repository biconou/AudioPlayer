package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

/**
 * Created by remi on 17/09/16.
 */
public class ConsoleLogPlayerListener implements PlayerListener {
    @Override
    public void nextStreamNotified(AudioInputStream nextAudioStream) {
        System.out.println("Start play audio stream : Format=["+nextAudioStream.getFormat().toString()+"]");
    }

    @Override
    public void endNotified() {

    }
}
