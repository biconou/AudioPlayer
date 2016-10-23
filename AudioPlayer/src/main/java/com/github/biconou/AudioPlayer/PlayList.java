package com.github.biconou.AudioPlayer;

import java.io.File;
import java.io.IOException;

public interface PlayList {
	
    File getNextAudioFile() throws IOException;

    File getCurrentAudioFile();

    int getSize();

    int getIndex();

}
