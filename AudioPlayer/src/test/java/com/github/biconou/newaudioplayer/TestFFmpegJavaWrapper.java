package com.github.biconou.newaudioplayer;

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

import com.github.biconou.newaudioplayer.audiostreams.AudioInputStreamUtils;
import com.github.biconou.newaudioplayer.config.Configuration;
import com.google.common.collect.ImmutableList;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.biconou.newaudioplayer.TestResourcesUtils.resourcesBasePath;

/**
 * Created by remi on 05/04/17.
 */
public class TestFFmpegJavaWrapper {


    @BeforeClass
    public static void setup() {
        Configuration.setFFmpegPath("/usr/bin/ffmpeg");
        Configuration.setFFprobePath("/usr/bin/ffprobe");
    }
    /**
     *
     */
    private FFprobe getFFprobe() throws IOException {
        return new FFprobe(Configuration.ffprobePath);
    }

    /**
     *
     */
    private FFmpeg getFFmpeg() throws IOException {
        return new FFmpeg(Configuration.ffmpegPath);
    }

    /**
     *
     */
    private FFmpegProbeResult getFFprobeResults(String filePath) throws IOException {
        FFprobe ffprobe = getFFprobe();
        FFmpegProbeResult probeResult = ffprobe.probe(filePath);
        return probeResult;
    }

    /**
     *
     */
    private Mixer resolveMixer() {
        String mixerName = "MID [plughw:0,0]";
        return AudioSystemUtils.findMixerByName(mixerName);
    }

    /**
     *
     */
    @Test
    public void ffprobeM4a() throws IOException {

        FFmpegProbeResult probeResult = getFFprobeResults(TestResourcesUtils.resolveFilePath("/M4A/example.m4a"));

        FFmpegFormat format = probeResult.getFormat();
        FFmpegStream stream = probeResult.getStreams().get(0);

        System.out.println(stream.codec_long_name);
        System.out.println(stream.sample_rate);
        System.out.println(stream.bits_per_raw_sample);
        System.out.println(stream.bits_per_sample);
        System.out.println(stream.channels);
    }




    /**
     *
     */
    @Test
    public void testPlayM4a() throws Exception {

        FFmpeg ffmpeg = getFFmpeg();

        String filePath = resourcesBasePath() + "/M4A/example.m4a";
        File file = new File(filePath);

        AudioInputStream originalAudioInputStream = AudioInputStreamUtils.getAudioInputStream(file);

        AudioFormat targetFormat = AudioInputStreamUtils.convertToPCMAudioFormat(originalAudioInputStream.getFormat());

        FFmpegBuilder ffmpegBuilder = createFFmpegBuilder(filePath, targetFormat);

        List<String> newArgs =
                ImmutableList.<String>builder().add(ffmpeg.getPath()).addAll(ffmpegBuilder.build()).build();

        Process p = new ProcessBuilder(newArgs).start();

        Mixer mixer = resolveMixer();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
        SourceDataLine dataLine = (SourceDataLine) mixer.getLine(info);
        dataLine.open(targetFormat);
        dataLine.start();

        InputStream in = p.getInputStream();
        int bytesPerSecond = (int) targetFormat.getFrameRate() * targetFormat.getFrameSize();
        System.out.println("bytesPerSecond : " + bytesPerSecond);
        byte[] buffer = new byte[bytesPerSecond];
        int byteRead = readOneSecond(in, buffer, bytesPerSecond);
        while (byteRead > 0) {
            System.out.println(Thread.currentThread().getName() + "write one buffer to line");
            dataLine.write(buffer, 0, byteRead);
            byteRead = readOneSecond(in, buffer, bytesPerSecond);
        }

        dataLine.drain();
        dataLine.close();
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


    /**
     *
     */
    private static int readOneSecond(InputStream stream, byte[] buffer, int bytesPerSecond) throws IOException {
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
