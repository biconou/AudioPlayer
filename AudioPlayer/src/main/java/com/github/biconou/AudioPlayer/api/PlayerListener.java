package com.github.biconou.AudioPlayer.api;

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
