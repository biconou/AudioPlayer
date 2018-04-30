package com.github.biconou.newaudioplayer.impl

import com.github.biconou.newaudioplayer.audiostreams.AudioInputStreamUtils
import java.nio.file.Path
import javax.sound.sampled.AudioInputStream

class PlayQueueItem {

    var audioFile: Path

    constructor(audioFile: Path) {
        this.audioFile = audioFile
    }
}