package com.github.biconou.AudioPlayer.api

import java.nio.file.Path

interface PlayQueue {

    fun add(filePath : Path)

    fun play()

    fun pause()

    fun stop()

    fun position(positionInSeconds: Int)
}