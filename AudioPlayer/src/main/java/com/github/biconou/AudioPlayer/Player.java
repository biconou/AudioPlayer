package com.github.biconou.AudioPlayer;


public interface Player {


	enum State {
	    PAUSED,
	    PLAYING,
        STOPPED,
	    CLOSED
	}

	State getState();

	void setPlayList(PlayList playList);

	void registerListener(PlayerListener listener);


    boolean isPlaying();

    boolean isPaused();

    void stop();

	void pause();

	void play() throws NothingToPlayException;
	
	void setGain(float gain);
	
	void close();

	void setPos(int i);

}