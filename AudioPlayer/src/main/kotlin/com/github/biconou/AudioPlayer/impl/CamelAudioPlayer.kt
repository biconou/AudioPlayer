package com.github.biconou.AudioPlayer.impl

import com.github.biconou.AudioPlayer.AudioSystemUtils
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*

class CamelAudioPlayer {

    private var context: DefaultCamelContext = DefaultCamelContext()
    private var mixer: Mixer
    private var dataLine: SourceDataLine? = null
    private var previousAudioFormat: AudioFormat? = null


    companion object {
        private val log = LoggerFactory.getLogger(CamelAudioPlayer.javaClass)
    }

    constructor(mixerName: String) {

        mixer = AudioSystemUtils.findMixerByName(mixerName)

        val dataProcessor = Processor { exchange ->
            val body = exchange!!.getIn().getBody(AudioDataMessage::class.java)
            when (body.type) {
                AudioDataMessageType.BEGIN -> {
                    log.debug("BEGIN")
                    prepareSourceDataLine(body.audioFormat)
                }
                AudioDataMessageType.END -> {
                    log.debug("END")
                }
                AudioDataMessageType.DATA -> {
                    log.debug("DATA")
                    this.dataLine?.write(body.data, 0, body.lengthInBytes)
                }
            }
        }

        val controlProcessor = Processor { exchange ->
            val body = exchange.`in`.getBody(ControlMessage::class.java)
            when (body.type) {
                ControlMessageType.PLAY -> {
                    log.debug("PLAY")
                    context.startRoute("dataRoute")
                }
                ControlMessageType.PAUSE -> {
                    log.debug("PAUSE")
                    // Stop data route immediately
                    context.stopRoute("dataRoute",1,TimeUnit.MILLISECONDS)
                }
                ControlMessageType.STOP -> {
                    log.debug("STOP")
                    context.stopRoute("dataRoute", 1, TimeUnit.MILLISECONDS)
                }
            }
        }

        context.addRoutes(object: RouteBuilder() {
            override fun configure() {
                from("vm:out?size=10").routeId("dataRoute").process(dataProcessor).noAutoStartup()
                from("vm:control").routeId("controlRoute").process(controlProcessor)
            }
        })
        context.start()
    }


    @Throws(LineUnavailableException::class)
    private fun prepareSourceDataLine(audioFormat: AudioFormat) {
        if (previousAudioFormat == null || audioFormat.toString() != previousAudioFormat.toString()) {
            log.debug("Start new source data line for audio format {}",audioFormat.toString())
            // TODO comparer ces autres syntaxes
            //dataLine?.let { dataLine -> dataLine.close() }
            //dataLine?.let { it.close() }
            dataLine?.close()
            val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            dataLine = mixer.getLine(info) as SourceDataLine
            dataLine?.open(audioFormat)
            dataLine?.start()
            previousAudioFormat = audioFormat
        }
    }

}