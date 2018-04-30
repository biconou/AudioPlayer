package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.ControlMessage

interface ControlChannel {
    fun pull(): ControlMessage
    fun push(controlMessage: ControlMessage)
    fun pause()
    fun play()
}