package com.github.biconou.AudioPlayer.api;


import com.github.biconou.AudioPlayer.NothingToPlayException;

public interface Player {


	enum State {
	    PAUSED,
	    PLAYING,
        STOPPED,
	    CLOSED
	}

    /**
     * Returns the current state of the player.
     * @return
     */
	State getState();

    /**
     * Replaces the current playlist by a given playlist object.
     *
     * @param playList
     */
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