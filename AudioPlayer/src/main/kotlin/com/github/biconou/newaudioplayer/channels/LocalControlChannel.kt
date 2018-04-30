package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.ControlMessage
import com.github.biconou.newaudioplayer.impl.ControlMessageType
import java.util.concurrent.LinkedBlockingQueue

class LocalControlChannel : ControlChannel {

    private val queue = LinkedBlockingQueue<ControlMessage>(10)

    override fun pull(): ControlMessage {
        return queue.take()
    }

    override fun push(controlMessage: ControlMessage) {
        queue.put(controlMessage)
    }

    override fun pause() {
        push(ControlMessage(ControlMessageType.PAUSE))
    }

    override fun play() {
        push(ControlMessage(ControlMessageType.PLAY))
    }
}