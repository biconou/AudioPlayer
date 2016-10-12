package com.github.biconou.AudioPlayer;


public interface Player {


	enum State {
	    PAUSED,
	    PLAYING,
	    CLOSED
	}

	State getState();

	void setPlayList(PlayList playList);

    void deletePlayList();

	void registerListener(PlayerListener listener);

    void addToPlayList(String filePath);

    boolean isPlaying();

    boolean isPaused();

    void stop();

	void pause();

	void play() throws NothingToPlayException;
	
	void setGain(float gain);
	
	void close();

	void setPos(int i);

}