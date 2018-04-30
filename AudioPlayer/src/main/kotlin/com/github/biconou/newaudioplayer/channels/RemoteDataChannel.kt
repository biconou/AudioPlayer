package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.DataMessage
import java.rmi.Remote
import java.rmi.RemoteException

interface RemoteDataChannel : Remote {

    // see : https://stackoverflow.com/questions/47737288/how-to-specify-throws-for-a-property-in-interface
    @Throws(RemoteException::class) fun push(audioDataMessage: DataMessage)
    @Throws(RemoteException::class) fun pull(): DataMessage
    @Throws(RemoteException::class) fun purge()
    @Throws(RemoteException::class) fun begin(audioFormatKey: String)
    @Throws(RemoteException::class) fun end(audioFormatKey: String)
}