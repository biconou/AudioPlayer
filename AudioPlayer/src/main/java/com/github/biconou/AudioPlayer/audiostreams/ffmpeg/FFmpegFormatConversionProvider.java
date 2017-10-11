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

import com.github.biconou.AudioPlayer.audiostreams.ffmpeg.FFmpegAudioInputStream;
import com.github.biconou.AudioPlayer.audiostreams.ffmpeg.FFmpegUtils;
import com.google.common.collect.ImmutableList;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FFmpegFormatConversionProvider extends FormatConversionProvider {

    @Override
    public AudioFormat.Encoding[] getSourceEncodings() {
        return new AudioFormat.Encoding[0];
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings() {
        return new AudioFormat.Encoding[0];
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
        return new AudioFormat.Encoding[0];
    }

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return new AudioFormat[0];
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
        return null;
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
        if (!(sourceStream instanceof FFmpegAudioInputStream)) {
            throw new RuntimeException();
        }
        FFmpegAudioInputStream fFmpegAudioInputStream = (FFmpegAudioInputStream)sourceStream;
        fFmpegAudioInputStream.setTargetAudioFormat(targetFormat);

        FFmpeg ffmpeg;
        try {
            ffmpeg = FFmpegUtils.resolveFFmpeg();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported conversion: " + targetFormat + " from " + sourceStream.getFormat());
        }
        FFmpegBuilder ffmpegBuilder = createFFmpegBuilder(fFmpegAudioInputStream.getAudioSourceFile().getAbsolutePath(), targetFormat);

        List<String> newArgs =
                ImmutableList.<String>builder().add(ffmpeg.getPath()).addAll(ffmpegBuilder.build()).build();

        Process p = null;
        try {
            p = new ProcessBuilder(newArgs).start();
        } catch (IOException e) {
            throw new RuntimeException("Error while executing ffmpeg",e);
        }

        InputStream inputStream = p.getInputStream();
        AudioInputStream resultAudioInputStream = new AudioInputStream(inputStream,targetFormat,sourceStream.getFrameLength());
        return resultAudioInputStream;
    }

    /**
     *
     */
    private FFmpegBuilder createFFmpegBuilder(String filepath, AudioFormat targetFormat) {

        int sampleSizeInBits = targetFormat.getSampleSizeInBits();
        String endian;
        if (targetFormat.isBigEndian()) {
            endian = "be";
        } else {
            endian = "le";
        }
        String format = "s"+sampleSizeInBits+endian;
        long audioBitRate = (long)targetFormat.getSampleRate();

        FFmpegBuilder ffmpegBuilder = new FFmpegBuilder()
                .setInput(filepath)
                .addStdoutOutput()
                .setFormat(format)
                .setAudioCodec("pcm_"+format)
                .setAudioBitRate(audioBitRate)
                .setAudioChannels(targetFormat.getChannels())
                .done();
        return ffmpegBuilder;
    }

}
