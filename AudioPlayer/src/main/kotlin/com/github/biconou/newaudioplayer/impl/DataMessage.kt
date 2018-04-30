package com.github.biconou.newaudioplayer.impl

import java.io.Serializable

data class DataMessage(val audioFormat: String, val data: ByteArray?, val lengthInBytes: Int, val type: AudioDataMessageType) : Serializable