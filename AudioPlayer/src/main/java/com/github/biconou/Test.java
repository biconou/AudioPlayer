package com.github.biconou;

import com.github.biconou.AudioPlayer.ArrayListPlayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

    private static Logger log = LoggerFactory.getLogger(Test.class);

    public void main(String[] args) {

        this.getClass().getResourceAsStream("/samples/moog_16_44100.wav");

        ArrayListPlayList playList = new ArrayListPlayList();


    }
}
