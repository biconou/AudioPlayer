package com.github.biconou.AudioPlayer;


public interface Player {


	public static enum State {
	    PAUSED,
	    PLAYING,
	    CLOSED
	}

	public State getState();

	public void setPlayList(PlayList playList);

	public void registerListener(PlayerListener listener);

	public void stop();

	public void pause();

	public void play() throws NothingToPlayException;
	
	public void setGain(float gain);
	
	public void close();

	void setPos(int i);

}