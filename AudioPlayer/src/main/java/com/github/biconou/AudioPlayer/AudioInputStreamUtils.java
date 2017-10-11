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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;

public class AudioInputStreamUtils {

    private static Logger log = LoggerFactory.getLogger(AudioInputStreamUtils.class);

    public static AudioFormat PCM_SIGNED_44100_16_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float) 44100,
            16,
            2,
            4,
            (float) 44100,
            false);

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

    public static AudioInputStream getAudioInputStream(File file) throws Exception {
        AudioInputStream sourceAudioInputStream;
        AudioInputStream targetAudioInputStream;

        sourceAudioInputStream = AudioSystemUtils.getAudioInputStream(file);

        if (sourceAudioInputStream == null) {
            throw new UnsupportedAudioFileException();
        }

        AudioFormat sourceAudioFormat = sourceAudioInputStream.getFormat();
        AudioFormat targetAudioFormat = convertToPCMAudioFormat(sourceAudioFormat);
        targetAudioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, sourceAudioInputStream);

        return targetAudioInputStream;
    }

}
