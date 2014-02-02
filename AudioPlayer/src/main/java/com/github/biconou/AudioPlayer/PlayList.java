package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

public interface PlayList {
	
	AudioInputStream getFirstAudioStream();
	
	String getFirstAudioFileName();
	
	AudioInputStream getNextAudioStream();
	
	String getNextAudioFileName();
	
	AudioInputStream getCurrentAudioStream();
	
	String getCurrentAudioFileName();

}
