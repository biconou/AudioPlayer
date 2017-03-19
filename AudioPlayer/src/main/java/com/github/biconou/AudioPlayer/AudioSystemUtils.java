package com.github.biconou.AudioPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Arrays;

/**
 * Created by remi on 19/03/17.
 */
public class AudioSystemUtils {

    public static Mixer.Info[] listAllMixers() {
        return AudioSystem.getMixerInfo();
    }
}
