package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

public interface PlayerListener {
	
	void nextStreamNotified(AudioInputStream nextAudioStream);
	
	void endNotified();

}
