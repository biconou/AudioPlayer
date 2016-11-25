package com.github.biconou.AudioPlayer;

import com.github.biconou.AudioPlayer.api.PlayerListener;

import java.io.File;

public class ConsoleLogPlayerListener implements PlayerListener {

    @Override
    public void onBegin(int index, File currentFile) {
        System.out.println("BEGIN : "+index+" : "+currentFile.getAbsolutePath());
    }

    @Override
    public void onEnd(int index, File currentFile) {
        System.out.println("END : "+index+" : "+currentFile.getAbsolutePath());
    }

    @Override
    public void onFinished() {
        System.out.println("FINISH");
    }

    @Override
    public void onStop() {
        System.out.println("STOP");
    }

    @Override
    public void onPause() {
        System.out.println("PAUSE");
    }
}
