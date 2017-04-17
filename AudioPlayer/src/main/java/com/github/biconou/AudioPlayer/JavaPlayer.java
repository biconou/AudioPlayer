package com.github.biconou.AudioPlayer;

/*-
 * #%L
 * AudioPlayer
 * %%
 * Copyright (C) 2016 - 2017 Rémi Cocula
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */



import com.github.biconou.AudioPlayer.api.PlayList;
import com.github.biconou.AudioPlayer.api.Player;
import com.github.biconou.AudioPlayer.api.PlayerListener;
import com.github.biconou.AudioPlayer.api.PlayingInfos;
import com.sun.media.sound.WaveExtensibleFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class JavaPlayer implements Player {

    public static final float DEFAULT_GAIN_VALUE = 0.5f;
    private static Logger log = LoggerFactory.getLogger(JavaPlayer.class);

    public static AudioFormat PCM_SIGNED_44100_16_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float) 44100,
            16,
            2,
            4,
            (float) 44100,
            false);

    public static AudioFormat PCM_SIGNED_96000_24_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float) 96000,
            24,
            2,
            6,
            (float) 96000,
            false);

    public static List<AudioFormat> allFormats = new ArrayList<>();

    static {
        allFormats.add(PCM_SIGNED_44100_16_LE);
        allFormats.add(PCM_SIGNED_96000_24_LE);
    }


    private static String computeFormatKey(AudioFormat format) {
        StringBuilder sb = new StringBuilder();
        sb.append(format.getEncoding()).append("_");
        sb.append((long) format.getSampleRate()).append("_");
        sb.append(format.getSampleSizeInBits()).append("_");
        if (format.isBigEndian()) {
            sb.append("BE");
        } else {
            sb.append("LE");
        }
        return sb.toString();
    }

    private static AudioFileReader[] audioFileReaders;

    static {
        audioFileReaders = new AudioFileReader[6];
        audioFileReaders[0] = new WaveFileReader();
        audioFileReaders[1] = new WaveFloatFileReader();
        audioFileReaders[2] = new WaveExtensibleFileReader();
        audioFileReaders[3] = new FlacAudioFileReader();
        audioFileReaders[4] = new MpegAudioFileReader();
        audioFileReaders[5] = new VorbisAudioFileReader();
    }


    private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);

    private DefaultPlayingInfosImpl infos = new DefaultPlayingInfosImpl();

    PlayList playList = null;
    List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    Boolean mustPause = Boolean.FALSE;
    Boolean mustStop = Boolean.FALSE;
    int pos = -1;
    AudioFormat previousUsedAudioFormat = null;
    private Mixer mixer;
    private Map<String, AudioFormat> supportedFormats = new HashMap<>();
    private SourceDataLineHolder dataLineHolder = null;
    float gain = DEFAULT_GAIN_VALUE;


    public JavaPlayer(Mixer mixer) {
        init(mixer);
    }


    public JavaPlayer(String mixerName) {
        Arrays.stream(AudioSystemUtils.listAllMixers()).forEach(info -> {
            if (info.getName().equals(mixerName)) {
                init(AudioSystem.getMixer(info));
            } else {
                throw new RuntimeException("No mixer named "+mixerName);
            }
        });
    }


    public JavaPlayer() {
        init(AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]));
    }

    /**
     * Initialisation of the player, done by the constructor.
     *
     * @param mixer The mixer chosen for this player.
     */
    private void init(Mixer mixer) {
        log.info("Player {} initialisation", this);
        log.info("mixer is : {} - {}", mixer.getMixerInfo().getName(), mixer.getMixerInfo().getDescription());
        this.mixer = mixer;

        allFormats.forEach(format -> {
            SourceDataLine dataLine = null;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!mixer.isLineSupported(info)) {
                log.info("Unsupported format : {}", format.toString());
            } else {
                try {
                    dataLine = (SourceDataLine) mixer.getLine(info);
                    dataLine.open(format);
                    dataLine.close();
                    supportedFormats.put(computeFormatKey(format), format);
                    log.info("Supported format : {}", format.toString());
                } catch (LineUnavailableException e) {
                    log.info("Unsupported format : {}", format.toString());
                }
            }
        });
        log.info("Player {} initialized", this);
    }

    private boolean isFormatSupported(AudioFormat format) {
        final boolean[] isSupported = {false};
        supportedFormats.keySet().forEach(s -> {
            if (computeFormatKey(format).equals(s)) isSupported[0] = true;
        });
        return isSupported[0];
    }

    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException {
        AudioInputStream audioInputStream = null;
        for (int i = 0; i < audioFileReaders.length; i++) {
            try {
                audioInputStream = audioFileReaders[i].getAudioInputStream(file);
                break;
            } catch (UnsupportedAudioFileException e) {
                // Continue to next reader
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (audioInputStream == null) {
            throw new UnsupportedAudioFileException();
        }

        AudioFormat audioFormat = audioInputStream.getFormat();
        log.debug("Raw format : {} ({})", audioFormat.toString(), file.getName());

        if (!isFormatSupported(audioFormat)) {
            int sampleSizeInBits = audioFormat.getSampleSizeInBits();
            float sampleRate = audioFormat.getSampleRate();

            AudioFormat targetFormat = PCM_SIGNED_44100_16_LE;

            if (sampleSizeInBits == 24 && sampleRate == (float) 96000) {
                if (isFormatSupported(PCM_SIGNED_96000_24_LE)) {
                    targetFormat = PCM_SIGNED_96000_24_LE;
                }
            }
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            log.debug("Converted to format : {}", targetFormat);
        }

        return audioInputStream;
    }


    public State getState() {
        return state.get();
    }


    public void setPlayList(PlayList playList) {
        if (isPlaying()) {
            stop();
        }
        this.playList = playList;
    }


    public void registerListener(PlayerListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean isPlaying() {
        return getState().equals(State.PLAYING);
    }

    @Override
    public boolean isPaused() {
        return getState().equals(State.PAUSED);
    }

    public void stop() {
        if (!getState().equals(State.CLOSED)) {
            if (isPlaying() || isPaused()) {
                notifyEvent(PlayerListener.Event.STOP);
                log.debug("Player {} : stop.", this);
                this.mustStop = Boolean.TRUE;
            }
            // wait until stop is complete
            while (!getState().equals(State.STOPPED)) ;
        }
    }

    private void doStop() {
        stopLine();
        mustStop = false;
        previousUsedAudioFormat = null;
        pos = -1;
        state.set(State.STOPPED);
    }

    public void pause() {
        mustPause = Boolean.TRUE;
        notifyEvent(PlayerListener.Event.PAUSE);
    }

    public void setPos(int posInSeconds) {
        this.pos = posInSeconds;
        infos.currentPosition = posInSeconds;
    }

    @Override
    public PlayingInfos getPlayingInfos() {
        return infos;
    }

    @Override
    public void play() throws NothingToPlayException {

        log.debug("Player {} : Play", this);

        if (playList == null) {
            log.debug("The playlist is null. Nothing to play");
            throw new NothingToPlayException();
        }

        if (isPlaying()) {
            throw new AlreadyPlayingException();
        }


        if (isPaused()) {
            log.debug("The player is paused. Try to resume");
            mustPause = Boolean.FALSE;
        } else {

            // start a thread that plays the audio streams from the play list.
            Thread playerThread = new Thread(() -> {
                AudioInputStream audioStreamToPlay;
                try {
                    audioStreamToPlay = getCurrentStreamFromPlayList();
                } catch (IOException|UnsupportedAudioFileException e) {
                    throw new RuntimeException(e);
                }

                while (audioStreamToPlay != null) {

                    infos.currentPosition = 0;
                    notifyEvent(PlayerListener.Event.BEGIN);

                    try {
                        AudioFormat audioFormat = audioStreamToPlay.getFormat();
                        log.debug("Audio Format is : {} ",audioFormat);
                        log.debug("Create audio buffers for this stream");
                        AudioBuffers audioBuffers = new AudioBuffers(audioStreamToPlay);
                        audioBuffers.fillBuffers();
                        log.debug("Audio buffers created");

                        pickADataLine(audioFormat);

                        boolean okToContinue = true;
                        while (okToContinue) {
                            if (mustStop) {
                                okToContinue = false;
                                doStop();
                            } else if (mustPause) {
                                state.set(State.PAUSED);
                            } else {
                                if (pos > -1) {
                                    log.debug("Go to position {} seconds",pos);
                                    try {
                                        audioBuffers.skip(pos);
                                    } catch (IllegalStateException e) {
                                        audioStreamToPlay = getCurrentStreamFromPlayList();
                                        audioBuffers = new AudioBuffers(audioStreamToPlay);
                                        audioBuffers.fillBuffers();
                                        audioBuffers.skip(pos);
                                    }

                                    pos = -1;
                                }
                                AudioBuffers.BufferHolder oneSecondBuffer = audioBuffers.getOneSecondOfMusic();
                                if (oneSecondBuffer != null) {
                                    state.set(State.PLAYING);
                                    // Write audio data to the line;
                                    dataLineHolder.getDataLine().write(oneSecondBuffer.buffer, 0, oneSecondBuffer.bytes);
                                    // Position is now one second ahead
                                    infos.currentPosition += 1;
                                } else {
                                    okToContinue = false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                    if (state.get() == State.STOPPED) {
                        audioStreamToPlay = null;
                    } else {
                        try {
                            notifyEvent(PlayerListener.Event.END);
                            audioStreamToPlay = getNextStreamFromPlayList();
                            if (audioStreamToPlay == null) {
                                notifyEvent(PlayerListener.Event.FINISHED);
                                doClose();
                            }
                        } catch (IOException|UnsupportedAudioFileException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }, this.getClass().getName() + ".Tplayer"); // The name of the Thread ends with Tplayer.

            playerThread.setPriority(Thread.MAX_PRIORITY);
            playerThread.start();
        }
    }

    @Override
    public void setGain(float gain) {
        this.gain = gain;
        if (dataLineHolder != null) {
            dataLineHolder.setGain(gain);
        }
    }

    @Override
    public float getGain() {
        return this.gain;
    }

    private void skip(int seconds, AudioInputStream stream, byte[] buffer, int bytesPerSecond) throws IOException {
        for (int i=0;i<seconds;i++) {
            AudioSystemUtils.readOneSecond(stream,buffer,bytesPerSecond);
        }
    }



    private void pickADataLine(AudioFormat audioFormat) throws LineUnavailableException {
        if (dataLineHolder == null || previousUsedAudioFormat == null || !audioFormat.toString().equals(previousUsedAudioFormat.toString())) {
            if (previousUsedAudioFormat != null) {
                log.debug("The previously used audio format was different. We will start a new line.");
            }
            previousUsedAudioFormat = audioFormat;
            if (dataLineHolder != null && dataLineHolder.getDataLine() != null) {
                log.debug("closing the current line.");
                dataLineHolder.getDataLine().close();
            }

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(info)) {
                throw new RuntimeException("Play.playAudioStream does not handle this type of audio on this system.");
            }

            SourceDataLine dataLine = (SourceDataLine) mixer.getLine(info);
            log.debug("A new line {} has been picked.", dataLine);
            dataLineHolder = new SourceDataLineHolder(dataLine,gain);

            try {
                dataLine.open(audioFormat);
                log.debug("dataline opened");
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }

            // Allows the line to move data in and out to a port.
            dataLine.start();
            log.debug("dataline started");
        }
    }


    private AudioInputStream getNextStreamFromPlayList() throws IOException, UnsupportedAudioFileException {
        log.debug("Player {} - getNextStreamFromPlayList : playlist count={} playlist index=", this, playList.getSize(), playList.getIndex());
        File file = playList.getNextAudioFile();
        if (file != null) {
            log.debug("Picking from play list : file {}.", file.getAbsolutePath());
            AudioInputStream audioStream = getAudioInputStream(file);
            return audioStream;
        } else {
            log.debug("Nothing picked from playlist.");
            return null;
        }
    }

    private AudioInputStream getCurrentStreamFromPlayList() throws IOException, UnsupportedAudioFileException {
        File file = playList.getCurrentAudioFile();
        if (file != null) {
            return getAudioInputStream(file);
        } else {
            return null;
        }
    }

    private void notifyEvent(PlayerListener.Event event) {
        int index = -1;
        File currentFile = null;
        if (playList != null) {
            index = playList.getIndex();
            currentFile = playList.getCurrentAudioFile();
        }
        for (PlayerListener listener : listeners) {
            switch (event) {
                case BEGIN:
                    listener.onBegin(index, currentFile);
                    break;
                case END:
                    listener.onEnd(index, currentFile);
                    break;
                case FINISHED:
                    listener.onFinished();
                    break;
                case PAUSE:
                    listener.onPause();
                    break;
                case STOP:
                    listener.onStop();
                    break;
            }
        }
    }

    private void stopLine() {
        if (dataLineHolder.getDataLine() != null) {
            dataLineHolder.getDataLine().drain();
            dataLineHolder.getDataLine().close();
        }
    }


    @Override
    public void close() {
        doStop();
        doClose();
    }

    private void doClose() {
        stopLine();
        dataLineHolder = null;
        gain = DEFAULT_GAIN_VALUE;
        state.set(State.CLOSED);
    }
}
