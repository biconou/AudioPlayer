package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.DataMessage
import java.rmi.registry.LocateRegistry
import javax.sound.sampled.AudioInputStream


class RemoteDataChannelAdapter(host: String) : DataChannel {

    var remoteDataChannel: RemoteDataChannel

    init {
        val registry = LocateRegistry.getRegistry(host)
        remoteDataChannel = registry.lookup("dataChannel") as RemoteDataChannel

    }

    override fun push(audioDataMessage: DataMessage) {
        remoteDataChannel.push(audioDataMessage)
    }

    override fun pull(): DataMessage {
        return remoteDataChannel.pull()
    }

    override fun purge() {
        remoteDataChannel.purge()
    }

    override fun begin(audioFormatKey: String) {
        remoteDataChannel.begin(audioFormatKey)
    }

    override fun end(audioFormatKey: String) {
        remoteDataChannel.end(audioFormatKey)
    }


}