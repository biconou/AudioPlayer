package com.github.biconou.AudioPlayer;


public interface Player {

	public static enum State {
	    PAUSED,
	    PLAYING,
	    CLOSED
	}

	/**
	 * Returns the player state.
	 */
	public abstract State getState();

	/**
	 * 
	 * @param palyList
	 */
	public abstract void setPlayList(PlayList playList);

	/**
	 * 
	 */
	public abstract void registerListener(PlayerListener listener);

	/**
	 * 
	 */
	public abstract void stop();

	/**
	 * 
	 */
	public abstract void pause();

	/** 
	 * Plays audio from the given audio input stream. 
	 * @param audioInputStream  The audio stream to play
	 */
	public abstract void play() throws NothingToPlayException; // playAudioStream
	
	public abstract void setGain(float gain);
	
	public abstract void close();

}