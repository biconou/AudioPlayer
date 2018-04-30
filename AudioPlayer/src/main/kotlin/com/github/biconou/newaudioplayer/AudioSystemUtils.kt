package com.github.biconou.newaudioplayer

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer


fun listAllMixers(): Array<Mixer.Info> {
    return AudioSystem.getMixerInfo()
}

fun findMixerByName(mixerName: String): Mixer? {
    val found = listAllMixers().filter { m -> m.name == mixerName }
    if (found.isEmpty()) {
        return null
    } else {
        return AudioSystem.getMixer(found.first())
    }
}
