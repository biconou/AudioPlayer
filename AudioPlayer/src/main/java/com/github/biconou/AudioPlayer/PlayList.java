package com.github.biconou.AudioPlayer;

import java.io.File;
import java.io.IOException;

public interface PlayList {
	
    File getNextAudioFile() throws IOException;

    File getCurrentAudioFile();

    void addToPlayList(File file);

    int getSize();

    int getIndex();

    void reset();

}
