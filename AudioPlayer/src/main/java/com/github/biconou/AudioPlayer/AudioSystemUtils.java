package com.github.biconou.AudioPlayer;

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
