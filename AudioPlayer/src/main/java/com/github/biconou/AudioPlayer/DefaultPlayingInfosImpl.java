package com.github.biconou.AudioPlayer;

import com.github.biconou.AudioPlayer.api.PlayingInfos;

class DefaultPlayingInfosImpl implements PlayingInfos {


    protected int currentPosition;

    @Override
    public int currentAudioPositionInSeconds() {
        return currentPosition;
    }
}
