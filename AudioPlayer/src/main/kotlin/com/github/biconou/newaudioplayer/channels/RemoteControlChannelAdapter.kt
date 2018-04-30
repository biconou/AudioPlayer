package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.ControlMessage
import java.rmi.Naming
import java.rmi.registry.LocateRegistry

class RemoteControlChannelAdapter(host: String): ControlChannel{

    var remoteControlChannel: RemoteControlChannel

    init {
        val registry = LocateRegistry.getRegistry(host)
        remoteControlChannel = registry.lookup("controlChannel") as RemoteControlChannel
    }

    override fun push(controlMessage: ControlMessage) {
        remoteControlChannel.push(controlMessage)
    }

    override fun pause() {
        remoteControlChannel.pause()
    }

    override fun play() {
        remoteControlChannel.play()
    }

    override fun pull(): ControlMessage {
        return remoteControlChannel.pull()
    }
}