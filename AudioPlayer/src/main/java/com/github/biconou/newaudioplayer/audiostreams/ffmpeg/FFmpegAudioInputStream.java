package com.github.biconou.newaudioplayer.audiostreams.ffmpeg;

/*-
 * #%L
 * newaudioplayer
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;

public class FFmpegAudioInputStream extends AudioInputStream {

    private static Logger log = LoggerFactory.getLogger(FFmpegAudioInputStream.class);

    private File audioSourceFile;

    private AudioFormat originalAudioFormat;
    private AudioFormat targetAudioFormat;


    public FFmpegAudioInputStream(File file, AudioFileFormat format, int frameLength) {
        super(null,format.getFormat(),frameLength);
        originalAudioFormat = format.getFormat();
        this.audioSourceFile = file;
    }


    public void setTargetAudioFormat(AudioFormat targetFormat) {
        this.targetAudioFormat = targetFormat;
    }

    @Override
    public AudioFormat getFormat() {
        if (targetAudioFormat == null) {
            return originalAudioFormat;
        } else {
            return targetAudioFormat;
        }
    }

    public File getAudioSourceFile() {
        return audioSourceFile;
    }
}
