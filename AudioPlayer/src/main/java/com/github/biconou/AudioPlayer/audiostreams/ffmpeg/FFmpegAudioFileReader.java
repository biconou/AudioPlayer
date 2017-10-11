package com.github.biconou.AudioPlayer.audiostreams.ffmpeg;

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

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FFmpegAudioFileReader extends AudioFileReader {

    private static final Logger log = LoggerFactory.getLogger(FFmpegAudioFileReader.class);

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {

        FFprobe ffprobe;
        try {
            ffprobe = FFmpegUtils.resolveFFprobe();
        } catch (Exception e) {
            UnsupportedAudioFileException ex = new UnsupportedAudioFileException("");
            ex.initCause(e);
            throw ex;
        }
        FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());

        if (probeResult.getStreams() == null) {
            throw new UnsupportedAudioFileException();
        }
        FFmpegStream stream = probeResult.getStreams().get(0);
        if (stream == null || !stream.codec_type.equals(FFmpegStream.CodecType.AUDIO)) {
            throw new UnsupportedAudioFileException();
        }
        float sampleRate = stream.sample_rate;
        int channels = stream.channels;
        int sampleSizeInBits = AudioSystem.NOT_SPECIFIED;
        int frameSize = AudioSystem.NOT_SPECIFIED;
        float frameRate = AudioSystem.NOT_SPECIFIED;
        boolean bigEndian = true;
        int frameLength = AudioSystem.NOT_SPECIFIED;

        // TODO se baser sur maxbitrate pour calculer sampleSizeInBits
        AudioFormat audioFormat = new AudioFormat(new DynamicFFprobeEncoding(stream), sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

        AudioFileFormat fileFormat = new AudioFileFormat(new DynamicAudioFileType(probeResult.getFormat()),audioFormat, frameLength);

        return fileFormat;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat format = getAudioFileFormat(file);
        return new FFmpegAudioInputStream(file,format,format.getFrameLength());
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }


}
