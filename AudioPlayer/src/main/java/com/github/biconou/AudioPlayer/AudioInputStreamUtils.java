package com.github.biconou.AudioPlayer;


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
