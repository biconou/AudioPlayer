package com.github.biconou.AudioPlayer.impl

import javax.sound.sampled.AudioFormat

data class AudioDataMessage(val audioFormat: AudioFormat, val data: ByteArray?, val lengthInBytes: Int, val type:AudioDataMessageType)