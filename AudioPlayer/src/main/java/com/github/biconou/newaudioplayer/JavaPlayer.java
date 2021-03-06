package com.github.biconou.newaudioplayer;

/*-
 * #%L
 * newaudioplayer
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



import com.github.biconou.newaudioplayer.api.PlayList;
import com.github.biconou.newaudioplayer.api.Player;
import com.github.biconou.newaudioplayer.api.PlayerListener;
import com.github.biconou.newaudioplayer.api.PlayingInfos;
import com.github.biconou.newaudioplayer.audiostreams.AudioInputStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class JavaPlayer implements Player {

    public static final float DEFAULT_GAIN_VALUE = 0.5f;
    private static Logger log = LoggerFactory.getLogger(JavaPlayer.class);


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



    private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);
    private DefaultPlayingInfosImpl infos = new DefaultPlayingInfosImpl();
    private PlayList playList = null;
    private List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    private Boolean mustPause = Boolean.FALSE;
    private Boolean mustStop = Boolean.FALSE;
    private int pos = -1;
    private AudioFormat previousUsedAudioFormat = null;
    private Mixer mixer;
    private Map<String, AudioFormat> supportedFormats = new HashMap<>();
    private SourceDataLineHolder dataLineHolder = null;
    private float gain = DEFAULT_GAIN_VALUE;


    public JavaPlayer(Mixer mixer) {
        init(mixer);
    }


    public JavaPlayer(String mixerName) {

        Mixer found = AudioSystemUtils.findMixerByName(mixerName);
        if (found != null) {
            init(found);
        } else {
            throw new RuntimeException("No mixer found with name "+mixerName);
        }
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

        log.info("Player {} initialized", this);
    }

    private boolean isFormatSupported(AudioFormat format) {
        final boolean[] isSupported = {false};
        supportedFormats.keySet().forEach(s -> {
            if (computeFormatKey(format).equals(s)) isSupported[0] = true;
        });
        return isSupported[0];
    }



    public State getState() {
        return state.get();
    }


    public void setPlayList(PlayList playList) {
        if (isPlaying() || isPaused()) {
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
                this.mustPause = Boolean.FALSE;
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
        infos.currentPosition = 0;
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                while (audioStreamToPlay != null) {

                    infos.currentPosition = 0;
                    notifyEvent(PlayerListener.Event.BEGIN);

                    try {

                        try {
                            pickADataLine(audioStreamToPlay);
                        } catch (LineUnavailableException e) {
                            log.warn("No line available for audio stream with format {}",audioStreamToPlay.getFormat().toString());
                            log.warn("Try to convert to {}", AudioInputStreamUtils.PCM_SIGNED_44100_16_LE);
                            audioStreamToPlay = AudioSystem.getAudioInputStream(AudioInputStreamUtils.PCM_SIGNED_44100_16_LE,audioStreamToPlay);
                            try {
                                pickADataLine(audioStreamToPlay);
                            } catch (LineUnavailableException e1) {
                                throw new RuntimeException(e1);
                            }
                        }

                        log.debug("Create audio buffers for this stream");
                        AudioBuffers audioBuffers = new AudioBuffers(audioStreamToPlay);
                        audioBuffers.fillBuffers();
                        log.debug("Audio buffers created");


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
                        } catch (Exception e) {
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


    private void pickADataLine(AudioInputStream audioInputStream) throws LineUnavailableException {

        AudioFormat audioFormat = audioInputStream.getFormat();

        if (dataLineHolder == null || previousUsedAudioFormat == null || !audioFormat.toString().equals(previousUsedAudioFormat.toString())) {
            if (previousUsedAudioFormat != null) {
                log.debug("The previously used audio format was different. We will start a new line.");
            }
            if (dataLineHolder != null) {
                dataLineHolder.close();
            }

            dataLineHolder = SourceDataLineHolder.open(mixer, audioInputStream, getGain());
            previousUsedAudioFormat = audioFormat;
        }
    }


    private AudioInputStream getNextStreamFromPlayList() throws Exception {
        log.debug("Player {} - getNextStreamFromPlayList : playlist count={} playlist index=", this, playList.getSize(), playList.getIndex());
        File file = playList.getNextAudioFile();
        if (file != null) {
            log.debug("Picking from play list : file {}.", file.getAbsolutePath());
            AudioInputStream audioStream = AudioInputStreamUtils.getPCMAudioInputStream(file);
            return audioStream;
        } else {
            log.debug("Nothing picked from playlist.");
            return null;
        }
    }

    private AudioInputStream getCurrentStreamFromPlayList() throws Exception {
        File file = playList.getCurrentAudioFile();
        log.debug("Pick file {} from play list",file.getAbsolutePath());
        if (file != null) {
            return AudioInputStreamUtils.getPCMAudioInputStream(file);
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
        log.debug("stopLine");
        if (dataLineHolder.getDataLine() != null) {
            dataLineHolder.getDataLine().drain();
            dataLineHolder.getDataLine().close();
        }
    }


    @Override
    public void close() {
        log.debug("close");
        if (!getState().equals(State.CLOSED)) {
            if (!getState().equals(State.STOPPED)) {
                stop();
            }
            doClose();
        }
    }

    private void doClose() {
        log.debug("doClose");
        stopLine();
        dataLineHolder = null;
        state.set(State.CLOSED);
    }
}
