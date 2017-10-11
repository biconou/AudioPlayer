package com.github.biconou.AudioPlayer.impl

import com.github.biconou.AudioPlayer.audiostreams.AudioInputStreamUtils
import java.nio.file.Path
import javax.sound.sampled.AudioInputStream

class PlayQueueItem {

    var audioFile: Path

    constructor(audioFile: Path) {
        this.audioFile = audioFile
    }
}