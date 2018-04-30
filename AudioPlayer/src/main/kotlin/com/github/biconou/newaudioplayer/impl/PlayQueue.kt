package com.github.biconou.newaudioplayer.impl

import com.github.biconou.newaudioplayer.audio.streams.bytesPerSecond
import com.github.biconou.newaudioplayer.audio.streams.readOneSecond
import com.github.biconou.newaudioplayer.audiostreams.AudioInputStreamUtils
import com.github.biconou.newaudioplayer.channels.ControlChannel
import com.github.biconou.newaudioplayer.channels.DataChannel
import com.github.biconou.newaudioplayer.computeFormatKey
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread


class PlayQueue(playerName: String) {

    companion object {
        private val log = LoggerFactory.getLogger(PlayQueue.javaClass)
    }

    private var dataChannel: DataChannel
    private var controlChannel: ControlChannel
    // N'est jamais utilisé donc on en a pas besoin. Il est ici uniquement pour garder une référence
    private var player: NewPlayer? = null
    private var dataProductionThread: Thread? = null

    // Attention cette déclaration d'objet doit être avant le init. Ce n'est pas très sécure.
    private val items = object {
        var currentIndex: Int = 0
        val list: CopyOnWriteArrayList<PlayQueueItem> = CopyOnWriteArrayList()

        fun add(filePath: Path) {
            list.add(PlayQueueItem(filePath))
        }

        fun current(): PlayQueueItem? {
            return try {
                list[currentIndex]
            } catch (e: IndexOutOfBoundsException) {
                null
            }
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

    init {
        log.info("Create a new PlayQueue with player named {}", playerName)

        val builder = PlayerBuilder(playerName)

        dataChannel = builder.dataChannel
        controlChannel = builder.controlChannel
        player = builder.player

        initDataProductionThread()
    } // end init


    private fun initDataProductionThread() {
        // Start producer thread
        if (dataProductionThread == null) {
            dataProductionThread = thread(start = true, name = "PlayQueue_producerThread") {
                var ok = true
                while (ok) {
                    // TODO virer cette boucle de merde et créer un itérateur sur la liste des items
                    items.current()?.run {
                        log.debug("PlayQueue : start sending DATA for {}", this.audioFile)
                        val audioStream = AudioInputStreamUtils.getPCMAudioInputStream(this.audioFile.toFile())

                        dataChannel.begin(audioStream.format.computeFormatKey())

                        // TODO faire de buffer un extension property
                        val buffer = ByteArray(audioStream.bytesPerSecond)
                        do {
                            // readOneSecond extension fuction
                            val bytesActuallyRead = audioStream.readOneSecond(buffer)
                            if (bytesActuallyRead > 0) dataChannel.push(DataMessage(audioStream.format.computeFormatKey(), buffer.copyOf(), bytesActuallyRead, AudioDataMessageType.DATA))
                        } while (bytesActuallyRead > 0)

                        // End of item AudioStream as been reached
                        dataChannel.end(audioStream.format.computeFormatKey())

                        log.debug("PlayQueue : end sending for {}", this.audioFile)
                        if (items.next() == null) ok = false
                    }
                }
            }
        }
    }




    fun add(filePath: Path) {
        log.debug("Add audio file {} to playqueue",filePath.toString())
        this.items.add(filePath)
    }


    fun play() {
        controlChannel.play()
    }

    fun pause() {
        controlChannel.pause()
    }

    fun stop() {
        controlChannel.pause()
        dataProductionThread!!.interrupt()
        dataProductionThread = null
        dataChannel.purge()
        initDataProductionThread()
    }

    fun position(positionInSeconds: Int) {
        throw UnsupportedOperationException()
    }
}