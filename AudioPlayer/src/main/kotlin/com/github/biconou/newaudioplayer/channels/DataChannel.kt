package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.DataMessage
import javax.sound.sampled.AudioInputStream

interface DataChannel {
    fun push(audioDataMessage: DataMessage)
    fun pull(): DataMessage
    fun purge()
    fun begin(audioFormatKey: String)
    fun end(audioFormatKey: String)
}