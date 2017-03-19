package com.github.biconou.AudioPlayer;

import com.github.biconou.AudioPlayer.api.PlayingInfos;

class DefaultPlayingInfosImpl implements PlayingInfos {


    protected int currentLength;
    protected int currentPosition;

    @Override
    public int currentAudioLengthInSeconds() {
        return currentLength;
    }

    @Override
    public int currentAudioPositionInSeconds() {
        return currentPosition;
    }
}
