package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

public class AudioStreamArrayPlayList implements PlayList {

	AudioInputStream[] audioStreams = null;
	int index = 0;
	
	/**
	 * 
	 * @param audioStreams
	 */
	public AudioStreamArrayPlayList(AudioInputStream[] audioStreams) {
		this.audioStreams = audioStreams;
	}
	
	/**
	 * 
	 */
	public AudioInputStream getFirstAudioStream() {
		return audioStreams[0];
	}

	/**
	 * 
	 */
	public AudioInputStream getNextAudioStream() {
		index ++;
		return getCurrentAudioStream();
	}

	/**
	 * 
	 */
	public AudioInputStream getCurrentAudioStream() {
		try {
			return audioStreams[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

}
