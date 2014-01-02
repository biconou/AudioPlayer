package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

public interface PlayList {
	
	AudioInputStream getFirstAudioStream();
	
	AudioInputStream getNextAudioStream();
	
	AudioInputStream getCurrentAudioStream();

}
