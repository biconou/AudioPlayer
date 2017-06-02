package com.github.biconou.AudioPlayer.api;

/*-
 * #%L
 * AudioPlayer
 * %%
 * Copyright (C) 2016 - 2017 Rémi Cocula
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */



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
     * @return the current state of the player.
     */
	State getState();

    /**
     * Sets the play list.
     *
     * If a play list is already present, replaces the current playlist by a given one.
	 *
     * If the player is currently playing or paused, it is stopped and then
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
	 * @param listener a listener to register.
	 */
	void registerListener(PlayerListener listener);

	/**
	 * Returns whether the player is currently playing.
	 * ie. is State {@link State#PLAYING}
	 *
	 * @return true if the player is playing.
	 */
    boolean isPlaying();

	/**
	 * Returns whether the player is currently paused.
	 * ie. is State {@link State#PAUSED}
	 *
	 * @return true if the player is paused.
	 */
    boolean isPaused();

	/**
	 * Stop the player.
     *
     * <ul>
	 * <li>If the player is currently closed, it remains closed and nothing happens.</li>
     * <li>If the player is currently stopped, it remains stopped and nothing happens.</li>
     * <li>If the player is currently playing or paused it stops and a {@link PlayerListener.Event#STOP} event is triggered.
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
	 * If the player is stopped or stopped this will start to play
	 * the current audio stream in play list from its beginning.
	 *
	 * It plays all songs in the play list. When the play list is finished,
	 * the player state is closed.
	 *
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
