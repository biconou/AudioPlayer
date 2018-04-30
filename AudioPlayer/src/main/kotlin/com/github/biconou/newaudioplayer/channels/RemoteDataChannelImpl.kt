package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.AudioDataMessageType
import com.github.biconou.newaudioplayer.impl.DataMessage
import java.rmi.server.UnicastRemoteObject

class RemoteDataChannelImpl : UnicastRemoteObject(), RemoteDataChannel {

    val localDataChannel = LocalDataChannel()

    override fun push(audioDataMessage: DataMessage) {
        localDataChannel.push(audioDataMessage)
    }

    override fun pull(): DataMessage {
        return localDataChannel.pull()
    }

    override fun purge() {
        localDataChannel.purge()
    }

    override fun begin(audioFormatKey: String) {
        localDataChannel.push(DataMessage(audioFormatKey, null, 0, AudioDataMessageType.BEGIN))
    }

    override fun end(audioFormatKey: String) {
        localDataChannel.push(DataMessage(audioFormatKey, null, 0, AudioDataMessageType.END))
    }
}