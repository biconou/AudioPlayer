package com.github.biconou.AudioPlayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArrayListPlayList implements PlayList {

	List<File> files = new ArrayList<>();
	int index = -1;


	public void addAudioFile(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		if (!file.canRead()) {
			throw new Exception("File "+path+" is not readable.");
		}
		this.files.add(file);
	}

	@Override
	public File getNextAudioFile() throws IOException {
		index ++;
		return getCurrentAudioFile();
	}

	@Override
	public File getCurrentAudioFile() {
		if (index > -1 && index < files.size()) {
			return files.get(index);
		} else {
			return null;
		}
	}

	@Override
	public void addToPlayList(File file) {
		files.add(file);
	}

	@Override
	public int getSize() {
		return files.size();
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void reset() {
		index = -1;
	}

}
