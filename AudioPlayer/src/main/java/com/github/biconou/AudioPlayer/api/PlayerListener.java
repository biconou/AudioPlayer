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

import java.io.File;

public interface PlayerListener {

    enum Event {
        BEGIN,
        END,
        FINISHED,
        STOP,
        PAUSE
    }

    /**
     * Triggered when a new stream begins to play.
     *
     * @param index The current index in play list (starts with 0)
     * @param currentFile The current file starting to play.
     */
	void onBegin(int index, File currentFile);

    /**
     * Triggered when a stream ends.
     *
     * @param index The current index in play list (starts with 0)
     * @param currentFile The current file that just ended play.
     */
    void onEnd(int index, File currentFile);

    /**
     * Triggered when the play list is exhausted.
     */
    void onFinished();

    /**
     * Triggered when the player has been stopped.
     */
    void onStop();

    /**
     * Triggered when the player has been paused.
     */
    void onPause();
}
