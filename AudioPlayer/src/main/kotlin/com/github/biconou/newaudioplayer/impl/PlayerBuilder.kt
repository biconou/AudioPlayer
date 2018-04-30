package com.github.biconou.newaudioplayer.impl

import com.github.biconou.newaudioplayer.channels.*


class PlayerBuilder(playerName: String) {

    var dataChannel: DataChannel
    var controlChannel: ControlChannel
    var player: NewPlayer? = null

    init {
        if (playerName.contains(char = '@')) {
            val splitted = playerName.split("@")
            val host = splitted.component2()
            controlChannel = RemoteControlChannelAdapter(host)
            dataChannel = RemoteDataChannelAdapter(host)
        } else {
            dataChannel = LocalDataChannel()
            controlChannel = LocalControlChannel()
            player = NewPlayer(playerName, dataChannel, controlChannel)
        }
    }
}