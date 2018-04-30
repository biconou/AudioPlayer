package com.github.biconou.newaudioplayer.channels

import com.github.biconou.newaudioplayer.impl.AudioDataMessageType
import com.github.biconou.newaudioplayer.impl.DataMessage
import java.util.concurrent.LinkedBlockingQueue

class LocalDataChannel : DataChannel {

    private val queue = LinkedBlockingQueue<DataMessage>(10)

    override fun push(audioDataMessage: DataMessage) {
        queue.put(audioDataMessage)
    }

    override fun pull(): DataMessage {
        return queue.take()
    }

    override fun purge() {
        queue.clear()
    }

    override fun begin(audioFormatKey: String) {
        push(DataMessage(audioFormatKey, null, 0, AudioDataMessageType.BEGIN))
    }

    override fun end(audioFormatKey: String) {
        push(DataMessage(audioFormatKey, null, 0, AudioDataMessageType.END))
    }
}