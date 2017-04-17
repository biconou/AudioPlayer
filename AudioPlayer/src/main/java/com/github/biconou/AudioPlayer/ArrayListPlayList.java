package com.github.biconou.AudioPlayer;

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

import com.github.biconou.AudioPlayer.api.PlayList;

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
        addAudioFile(file);
	}

	public void addAudioFile(File file) {
		if (file == null) {
			throw new RuntimeException("Can not add null file");
		}
		this.files.add(file);
		if (index == -1) {
			index = 0;
		}
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
	public int getSize() {
		return files.size();
	}

	@Override
	public int getIndex() {
		return index;
	}

}
