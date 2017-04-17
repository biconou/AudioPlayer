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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by remi on 19/03/17.
 */
public class AudioSystemUtils {

    public static Mixer.Info[] listAllMixers() {
        return AudioSystem.getMixerInfo();
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
