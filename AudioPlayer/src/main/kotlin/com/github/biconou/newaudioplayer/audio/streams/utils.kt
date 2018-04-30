package com.github.biconou.newaudioplayer.audio.streams

import java.io.IOException
import javax.sound.sampled.AudioInputStream


val AudioInputStream.bytesPerSecond: Int
    get() = format.sampleRate.toInt() * format.frameSize

@Throws(IOException::class)
// TODO utiliser une paire pour le type retour
fun AudioInputStream.readOneSecond(buffer: ByteArray): Int {
    var bytes: Int
    var totalBytesRead = 0
    val bytesPerSecond = this.bytesPerSecond
    while (totalBytesRead < bytesPerSecond) {
        bytes = this.read(buffer, totalBytesRead, bytesPerSecond - totalBytesRead)
        if (totalBytesRead == 0 && bytes == -1) {
            return -1
        }
        if (bytes != -1) {
            totalBytesRead += bytes
        } else {
            return totalBytesRead
        }
    }
    return totalBytesRead
}