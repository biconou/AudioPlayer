package com.github.biconou.AudioPlayer.api;


import com.github.biconou.AudioPlayer.AlreadyPlayingException;
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
     *
     * @return
     */
	State getState();

    /**
     * Sets the play list.
     *
     * If a play list is already present, replaces the current playlist by a given one.
	 *
     * If the player is currently playing, it is stopped and then
     * the current audio stream to be played is now the first one of the new play list.
     *
     * Anyway, after a call to this method, the player remains stopped.
     *
     *
     * @param playList a play list object. Can be null to erase the current play list.
     */
	void setPlayList(PlayList playList);

	/**
	 * Register a new listener that will be notified when certain events
	 * are triggered during playing.
	 *
	 * @param listener
	 */
	void registerListener(PlayerListener listener);

	/**
	 * Returns whether the player is currently playing.
	 * ie. is State {@link State#PLAYING}
	 *
	 * @return
	 */
    boolean isPlaying();

	/**
	 * Returns whether the player is currently paused.
	 * ie. is State {@link State#PAUSED}
	 *
	 * @return
	 */
    boolean isPaused();

	/**
	 * Stop the player.
     *
     * <ul>
	 * <li>If the player is currently closed, it remains closed and nothing happens.</li>
     * <li>If the player is currently stopped, it remains stopped and nothing happens.</li>
     * <li>If the player is currently playing it stops and a {@link PlayerListener.Event#STOP} event is triggered.
     * The current audio streams in the play list remains the latest being played.</li>
     * </ul>
	 */
	void stop();

	/**
	 * Pause the player.
	 * Nothing happens if the player is not currently playing.
	 */
	void pause();

	/**
	 * Starts playing.
	 * If the playing is currently paused, it resumes the playing.
	 * If the player is stopped, this will start to play the current audio stream in play list.
	 *
	 * @throws NothingToPlayException if the play list is empty.
     * @throws AlreadyPlayingException if the player is already playing.
	 */
	void play() throws NothingToPlayException, AlreadyPlayingException;

    /**
     * Attempt to set the global gain (volume ish) for the play back. If the
     * control is not supported this method has no effect. 1.0 will set maximum
     * gain, 0.0 minimum gain.
     *
     * The initial value of gain is 0.5.
     *
     * @param gain The gain value
     */
    void setGain(float gain);

    float getGain();

    /**
     * Stops the player and closes all resources.
     */
	void close();

    /**
     * If the player is currently playing, skip to a position in seconds inside the
     * current audio stream.
     *
     * @param i
     */
	void setPos(int i);

	PlayingInfos getPlayingInfos();
}