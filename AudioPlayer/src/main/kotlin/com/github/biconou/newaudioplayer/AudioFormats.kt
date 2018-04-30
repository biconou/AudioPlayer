package com.github.biconou.newaudioplayer

import javax.sound.sampled.AudioFormat


fun AudioFormat.computeFormatKey(): String {
    val sb = StringBuilder()
    sb.append(encoding).append("_")
    sb.append(sampleRate.toLong()).append("_")
    sb.append(sampleSizeInBits).append("_")
    if (isBigEndian) {
        sb.append("BE")
    } else {
        sb.append("LE")
    }
    return sb.toString()
}

object supportedAudioFormats {
    val formats = listOf(
            AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100.toFloat(),
                    16,
                    2,
                    4,
                    44100.toFloat(),
                    false)
    )

    private val formatsMap = mutableMapOf<String, AudioFormat>()

    init {
        with(formatsMap) {
            formats.forEach { put(it.computeFormatKey(), it) }
        }
    }

    fun findFormat(formatKey: String): AudioFormat? {
        return formatsMap[formatKey]
    }
}