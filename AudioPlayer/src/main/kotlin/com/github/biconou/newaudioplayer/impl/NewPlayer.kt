package com.github.biconou.newaudioplayer.impl

import com.github.biconou.newaudioplayer.channels.ControlChannel
import com.github.biconou.newaudioplayer.channels.DataChannel
import com.github.biconou.newaudioplayer.channels.LocalControlChannel
import com.github.biconou.newaudioplayer.channels.LocalDataChannel
import com.github.biconou.newaudioplayer.findMixerByName
import com.github.biconou.newaudioplayer.supportedAudioFormats
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

class NewPlayer(playerName: String, dataChannel: DataChannel, controlChannel: ControlChannel) {

    companion object {
        private val log = LoggerFactory.getLogger(NewPlayer.javaClass)
    }

    // TODO ici dans le cas où n'est pas trouvé, ça devrait planter
    private val mixer = findMixerByName(playerName)
    // TODO utiliser lateinit ?
    private var dataLine: SourceDataLine? = null
    private val lock = Object()
    private var mustWait: Boolean = true

    val controlThread = thread(start = true, name = "player_control_thread") {
        while (true) {
            val controlMessage = controlChannel.pull()
            when (controlMessage.type) {
                ControlMessageType.PLAY -> synchronized(lock) {
                    mustWait = false
                    lock.notify()
                }
                ControlMessageType.PAUSE -> synchronized(lock) {
                    mustWait = true
                }
            }
        }
    }

    val dataThread = thread(start = true, name = "player_data_thread") {
        while (true) {
            if (mustWait) {
                synchronized(lock) {
                    lock.wait()
                }
            }
            val dataMessage = dataChannel.pull()
            when (dataMessage.type) {
                AudioDataMessageType.BEGIN -> {
                    val format = supportedAudioFormats.findFormat(dataMessage.audioFormat)
                    if (format != null) {
                        prepareSourceDataLine(format)
                    } else {
                        throw RuntimeException("format $format is not supported")
                    }
                }
                AudioDataMessageType.DATA -> this.dataLine?.write(dataMessage.data, 0, dataMessage.lengthInBytes)
            }
        }
    }

    @Throws(LineUnavailableException::class)
    private fun prepareSourceDataLine(audioFormat: AudioFormat) {
        log.debug("Start new source data line for audio format {}", audioFormat.toString())
        dataLine?.close()
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        dataLine = mixer?.getLine(info) as SourceDataLine
        dataLine?.open(audioFormat)
        dataLine?.start()
    }

}