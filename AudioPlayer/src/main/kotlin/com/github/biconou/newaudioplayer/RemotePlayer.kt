package com.github.biconou.newaudioplayer

import com.github.biconou.newaudioplayer.channels.RemoteControlChannelImpl
import com.github.biconou.newaudioplayer.channels.RemoteDataChannelImpl
import com.github.biconou.newaudioplayer.impl.NewPlayer
import java.rmi.registry.LocateRegistry


fun main(args: Array<String>) {

    val dataChannel = RemoteDataChannelImpl()
    val controlChannel = RemoteControlChannelImpl()

    val registry = LocateRegistry.createRegistry(1099);

    registry.bind("dataChannel", dataChannel)
    registry.bind("controlChannel", controlChannel)
    registry.list().forEach { println(it) }

    NewPlayer("default [default]", dataChannel = dataChannel.localDataChannel, controlChannel = controlChannel.localControlChannel)

    println("Serveur lancé")
}