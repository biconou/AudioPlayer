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

import com.sun.media.sound.JDK13Services;
import com.sun.media.sound.WaveExtensibleFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.jflac.sound.spi.FlacAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by remi on 19/03/17.
 */
public class AudioSystemUtils {

    private static List<AudioFileReader> audioFileReaders;

    static {

        audioFileReaders = new ArrayList<>();
        audioFileReaders.add(new WaveFileReader());
        audioFileReaders.add(new WaveFloatFileReader());
        audioFileReaders.add(new WaveExtensibleFileReader());
        audioFileReaders.add(new AACAudioFileReader());

        List foundAudioFileReaders = JDK13Services.getProviders(AudioFileReader.class);
        foundAudioFileReaders.stream().forEach(reader -> {
            if (!(reader instanceof WaveFileReader) &&
                    !(reader instanceof WaveFloatFileReader) &&
                        !(reader instanceof WaveExtensibleFileReader)) {
                audioFileReaders.add((AudioFileReader) reader);
            }
        });
    }


    public static AudioInputStream getAudioInputStream(File file)
            throws UnsupportedAudioFileException, IOException {

        AudioInputStream audioStream = null;

        for(int i = 0; i < audioFileReaders.size(); i++ ) {
            AudioFileReader reader = audioFileReaders.get(i);
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


    public static Mixer.Info[] listAllMixers() {
        return AudioSystem.getMixerInfo();
    }

    public static Mixer findMixerByName(String mixerName) {

        Mixer.Info[] allMixers = AudioSystemUtils.listAllMixers();

        Predicate<Mixer.Info> p = mixer -> mixer.getName().equals(mixerName);
        Mixer.Info found = Arrays.stream(allMixers).filter(p).findFirst().get();
        if (found != null) {
            return AudioSystem.getMixer(found);
        } else {
            return null;
        }
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
