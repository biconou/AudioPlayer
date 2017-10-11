package com.github.biconou.AudioPlayer.impl

import com.github.biconou.AudioPlayer.api.PlayQueue
import com.github.biconou.AudioPlayer.audio.streams.bytesPerSecond
import com.github.biconou.AudioPlayer.audio.streams.readOneSecond
import com.github.biconou.AudioPlayer.audiostreams.AudioInputStreamUtils
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread


class PlayQueueImpl : PlayQueue {

    companion object {
        private val log = LoggerFactory.getLogger(PlayQueueImpl.javaClass)

        fun createPlayQueue(mixerName: String): PlayQueue {
            return PlayQueueImpl(mixerName)
        }
    }

    private val items = object {
        var currentIndex: Int = -1
        val list: CopyOnWriteArrayList<PlayQueueItem> = CopyOnWriteArrayList()

        fun add(filePath: Path) {
            list.add(PlayQueueItem(filePath))
        }

        fun next(): PlayQueueItem? {
            return try {
                currentIndex++
                list[currentIndex]
            } catch (e: IndexOutOfBoundsException) {
                currentIndex--
                null
            }
        }
    }

    private val context: CamelContext = DefaultCamelContext()
    private val template = context.createProducerTemplate()
    private val player: CamelAudioPlayer

    constructor(mixerName: String) {

        // Create a player
        player = CamelAudioPlayer(mixerName)

        // TODO
        // Au lieu de tout ce qui suit, créer une méthode startDataRoute(itemIndex, position)
        // Qui démarre une route en alimantant les données depuis un iterateur (ou une collection ?)
        context.addRoutes(object : RouteBuilder() {
            override fun configure() {
                from("direct:in").id("dataRoute").to("vm:out?size=10&blockWhenFull=true")
            }
        })
        context.start()

        // Start producer thread
        thread (start = true, name = "PlayQueueImpl_producerThread") {
            while (true) {
                items.next()?.run {
                    log.debug("PlayQueue : start sending DATA for {}", this.audioFile)
                    val audioStream = AudioInputStreamUtils.getPCMAudioInputStream(this.audioFile.toFile())
                    template.sendBody("direct:in", AudioDataMessage(audioStream.format, null, 0, AudioDataMessageType.BEGIN))
                    // TODO faire de buffer un extension property
                    val buffer = ByteArray(audioStream.bytesPerSecond)
                    do {
                        // readOneSecond extension fuction
                        val bytesActuallyRead = audioStream.readOneSecond(buffer)
                        if (bytesActuallyRead > 0) template.sendBody("direct:in", AudioDataMessage(audioStream.format, buffer.copyOf(), bytesActuallyRead, AudioDataMessageType.DATA))
                    } while (bytesActuallyRead > 0)
                    // End of item AudioStream as been reached
                    template.sendBody("direct:in", AudioDataMessage(audioStream.format, null, 0, AudioDataMessageType.END))
                    log.debug("PlayQueue : end sending for {}", this.audioFile)
                }
            }
        }
    }

    override fun add(filePath: Path) {
        log.debug("Add audio file {} to playqueue",filePath.toString())
        this.items.add(filePath)
    }


    override fun play() {
        template.sendBody("vm:control", ControlMessage(ControlMessageType.PLAY))
    }

    override fun pause() {
        template.sendBody("vm:control", ControlMessage(ControlMessageType.PAUSE))
    }

    override fun stop() {
        template.sendBody("vm:control", ControlMessage(ControlMessageType.STOP))
        position(0)
    }

    override fun position(positionInSeconds: Int) {
        template.sendBody("vm:control", ControlMessage(ControlMessageType.STOP))
        context.stopRoute("dataRoute")
        context.removeRoute("dataRoute")
        TODO("Purger la data queue")
        template.sendBody("vm:control", ControlMessage(ControlMessageType.PLAY))
    }
}