package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.ControlMessage
import java.rmi.Remote
import java.rmi.RemoteException

interface RemoteControlChannel : Remote {
    @Throws(RemoteException::class) fun pull(): ControlMessage
    @Throws(RemoteException::class) fun push(controlMessage: ControlMessage)
    @Throws(RemoteException::class) fun pause()
    @Throws(RemoteException::class) fun play()
}