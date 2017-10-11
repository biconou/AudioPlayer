package com.github.biconou.AudioPlayer.audiostreams;

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


import com.github.biconou.AudioPlayer.audiostreams.ffmpeg.FFmpegAudioFileReader;
import com.github.biconou.AudioPlayer.audiostreams.ffmpeg.FFmpegAudioInputStream;
import com.github.biconou.AudioPlayer.audiostreams.ffmpeg.FFmpegFormatConversionProvider;
import com.sun.media.sound.JDK13Services;
import com.sun.media.sound.WaveExtensibleFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioInputStreamUtils {

    private static Logger log = LoggerFactory.getLogger(AudioInputStreamUtils.class);

    private static List<AudioFileReader> audioFileReaders;
    static {
        audioFileReaders = new ArrayList<>();
        audioFileReaders.add(new WaveFileReader());
        audioFileReaders.add(new WaveFloatFileReader());
        audioFileReaders.add(new WaveExtensibleFileReader());

        List foundAudioFileReaders = JDK13Services.getProviders(AudioFileReader.class);
        foundAudioFileReaders.stream().forEach(reader -> {
            if (!(reader instanceof WaveFileReader) &&
                    !(reader instanceof WaveFloatFileReader) &&
                    !(reader instanceof WaveExtensibleFileReader)) {
                audioFileReaders.add((AudioFileReader) reader);
            }
        });
        audioFileReaders.add(new FFmpegAudioFileReader());
    }

    private static FFmpegFormatConversionProvider ffmpegFormatConversionProvider = new FFmpegFormatConversionProvider();

    public static AudioFormat PCM_SIGNED_44100_16_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float) 44100,
            16,
            2,
            4,
            (float) 44100,
            false);

    @Deprecated
    public static AudioFormat PCM_SIGNED_96000_24_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float) 96000,
            24,
            2,
            6,
            (float) 96000,
            false);


    public static AudioFormat convertToPCMAudioFormat(AudioFormat sourceFormat) {

        AudioFormat targetFormat = sourceFormat;
        log.debug("Original audio format is {} ",sourceFormat.toString());

        if (!sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {

            float targetFrameRate = sourceFormat.getSampleRate();
            int targetSampleSizeInBits = sourceFormat.getSampleSizeInBits();
            if (targetSampleSizeInBits == -1) {
                targetSampleSizeInBits = 16;
            }
            int targetChannels = sourceFormat.getChannels();
            int targetFrameSize = (int)(targetChannels * targetSampleSizeInBits / 8);


            targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    targetSampleSizeInBits,
                    targetChannels,
                    targetFrameSize,
                    targetFrameRate,
                    false);

            log.debug("Converted audio format is {} ",targetFormat.toString());

        }
        return targetFormat;
    }

    /**
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static AudioInputStream getPCMAudioInputStream(File file) throws Exception {
        AudioInputStream sourceAudioInputStream;
        AudioInputStream targetAudioInputStream;

        sourceAudioInputStream = getAudioInputStream(file);

        if (sourceAudioInputStream == null) {
            throw new UnsupportedAudioFileException();
        }

        AudioFormat sourceAudioFormat = sourceAudioInputStream.getFormat();
        AudioFormat targetAudioFormat = convertToPCMAudioFormat(sourceAudioFormat);
        if (sourceAudioInputStream instanceof FFmpegAudioInputStream) {
            targetAudioInputStream = ffmpegFormatConversionProvider.getAudioInputStream(targetAudioFormat,sourceAudioInputStream);
        } else {
            targetAudioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, sourceAudioInputStream);
        }

        return targetAudioInputStream;
    }

    /**
     * This is a replacement of the standard {@link AudioSystem::getAudioInputStream}
     * method.
     *
     * @param file
     * @return
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public static AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException {

        List providers = audioFileReaders;
        AudioInputStream audioStream = null;

        for(int i = 0; i < providers.size(); i++ ) {
            AudioFileReader reader = (AudioFileReader) providers.get(i);
            try {
                audioStream = reader.getAudioInputStream( file ); // throws IOException
                break;
            } catch (UnsupportedAudioFileException e) {
                continue;
            }
        }

        if( audioStream==null ) {
            throw new UnsupportedAudioFileException("could not get audio input stream from input file");
        } else {
            return audioStream;
        }
    }

    public static int computeBytesPerSecond(AudioInputStream stream) {
        return (int) stream.getFormat().getSampleRate() * stream.getFormat().getFrameSize();
    }


    public static int readOneSecond(AudioInputStream stream, byte[] buffer, int bytesPerSecond) throws IOException {
        int bytes;
        int totalBytesRead = 0;
        while (totalBytesRead < bytesPerSecond) {
            bytes = stream.read(buffer, totalBytesRead, bytesPerSecond - totalBytesRead);
            if (totalBytesRead == 0 && bytes == -1) {
                return -1;
            }
            if (bytes != -1) {
                totalBytesRead += bytes;
            } else {
                return totalBytesRead;
            }
        }
        return totalBytesRead;
    }

}
