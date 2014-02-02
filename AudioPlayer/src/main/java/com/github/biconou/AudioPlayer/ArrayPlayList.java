package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioInputStream;

public class ArrayPlayList implements PlayList {

	AudioInputStream[] audioStreams = null;
	String[] filesNames = null;
	int index = 0;
	
	/**
	 * 
	 * @param audioStreams
	 */
	public ArrayPlayList(AudioInputStream[] audioStreams) {
		this.audioStreams = audioStreams;
	}

	/**
	 * 
	 * @param audioStreams
	 */
	public ArrayPlayList(String[] files) {
		this.filesNames = files;
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
	
	

	public String getFirstAudioFileName() {
		return filesNames[0];
	}

	public String getNextAudioFileName() {
		index ++;
		return getCurrentAudioFileName();
	}

	public String getCurrentAudioFileName() {
		try {
			return filesNames[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

}
