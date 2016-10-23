package com.github.biconou.AudioPlayer;


import com.sun.media.sound.WaveExtensibleFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class JavaPlayer implements Player {

    private static Logger log = LoggerFactory.getLogger(JavaPlayer.class);

    public static AudioFormat PCM_SIGNED_44100_16_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float)44100,
            16,
            2,
            4,
            (float)44100,
            false);

    public static AudioFormat PCM_SIGNED_96000_24_LE = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            (float)96000,
            24,
            2,
            6,
            (float)96000,
            false);

    public static List<AudioFormat> allFormats = new ArrayList<>();

    static {
        allFormats.add(PCM_SIGNED_44100_16_LE);
        allFormats.add(PCM_SIGNED_96000_24_LE);
    }


    private static String computeFormatKey(AudioFormat format) {
        StringBuilder sb = new StringBuilder();
        sb.append(format.getEncoding()).append("_");
        sb.append((long)format.getSampleRate()).append("_");
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
        audioFileReaders = new AudioFileReader[5];
        audioFileReaders[0] = new WaveFileReader();
        audioFileReaders[1] = new WaveFloatFileReader();
        audioFileReaders[2] = new WaveExtensibleFileReader();
        audioFileReaders[3] = new FlacAudioFileReader();
        audioFileReaders[4] = new MpegAudioFileReader();
    }


    private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);

    PlayList playList = null;
    SourceDataLine dataLine = null;
    List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    Boolean mustPause = Boolean.FALSE;
    Boolean mustStop = Boolean.FALSE;
    int pos = -1;
    AudioFormat previousUsedAudioFormat = null;
    private Mixer mixer;
    private Map<String,AudioFormat> supportedFormats = new HashMap<>();

    public JavaPlayer(Mixer mixer) {
        init(mixer);
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
        log.info("Player {} initialisation",this);
        log.info("mixer is : {} - {}",mixer.getMixerInfo().getName(),mixer.getMixerInfo().getDescription());
        this.mixer = mixer;

        allFormats.forEach(format -> {
            SourceDataLine dataLine = null;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!mixer.isLineSupported(info)) {
                log.info("Unsupported format : {}",format.toString());
            } else {
                try {
                    dataLine = (SourceDataLine) mixer.getLine(info);
                    dataLine.open(format);
                    dataLine.close();
                    supportedFormats.put(computeFormatKey(format),format);
                    log.info("Supported format : {}",format.toString());
                } catch (LineUnavailableException e) {
                    log.info("Unsupported format : {}",format.toString());
                }
            }
        });
        log.info("Player {} initialized",this);
    }

    private boolean isFormatSupported(AudioFormat format) {
        final boolean[] isSupported = {false};
        supportedFormats.keySet().forEach(s -> {if (computeFormatKey(format).equals(s)) isSupported[0] = true;});
        return isSupported[0];
    }

    public AudioInputStream getAudioInputStream(File file) {
        AudioInputStream audioInputStream = null;
        for (int i=0;i<audioFileReaders.length;i++) {
            try {
                audioInputStream = audioFileReaders[i].getAudioInputStream(file);
                break;
            } catch (UnsupportedAudioFileException e) {
                // Continue to next reader
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        AudioFormat audioFormat = audioInputStream.getFormat();
        log.debug("Raw format : {} ({})",audioFormat.toString(),file.getName());

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
            log.debug("Converted to format : {}",targetFormat);
        }

        return audioInputStream;
    }


    public State getState() {
        return state.get();
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }


    public void registerListener(PlayerListener listener) {
        listeners.add(listener);
    }


    /**
     * Returns whether the player is playing music.
     * @return
     */
    @Override
    public boolean isPlaying() {
        return getState().equals(State.PLAYING);
    }

    /**
     * Returns whether the player is paused at the moment.
     * @return
     */
    @Override
    public boolean isPaused() {
        return getState().equals(State.PAUSED);
    }

    public void stop() {
        if (!getState().equals(State.CLOSED)) {
            notifyEvent(PlayerListener.Event.STOP);
            if (isPlaying() || isPaused()) {
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
    }

    /**
     * Starts playing or resume playing if the player was paused.
     *
     * @throws NothingToPlayException
     */
    public void play() throws NothingToPlayException {

        log.debug("Player {} : Play",this);

        if (getState().equals(State.PAUSED)) {
            log.debug("The player is paused. Try to resume");
            mustPause = Boolean.FALSE;
        } else {

            if (playList == null) {
                log.debug("The playlist is null. Nothing to play");
                throw new NothingToPlayException();
            }

            // start a thread that plays the audio streams from the play list.
            Thread playerThread = new Thread(() -> {
                AudioInputStream audioStreamToPlay = null;
                try {
                    // Getting the next stream here returns the first one in play list.
                    audioStreamToPlay = getNextStreamFromPlayList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                while (audioStreamToPlay != null) {

                    state.set(State.PLAYING);
                    notifyEvent(PlayerListener.Event.BEGIN);

                    // Audio format provides information like sample rate, size, channels.
                    AudioFormat audioFormat = audioStreamToPlay.getFormat();
                    try {
                        if (previousUsedAudioFormat == null || !audioFormat.toString().equals(previousUsedAudioFormat.toString())) {
                            if (previousUsedAudioFormat != null) {
                                log.debug("The previously used audio format was different. We will start a new line.");
                            }
                            previousUsedAudioFormat = audioFormat;
                            if (dataLine != null) {
                                log.debug("closing the current line.");
                                dataLine.close();
                            }

                            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                            if (!AudioSystem.isLineSupported(info)) {
                                throw new RuntimeException("Play.playAudioStream does not handle this type of audio on this system.");
                            }

                            dataLine = (SourceDataLine) mixer.getLine(info);
                            log.debug("A new line {} has been picked.",dataLine);

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

                        final int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
                        byte[] buffer = new byte[bufferSize];
                        int bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
                        while (bytes != -1) {
                            if (mustStop) {
                                bytes = -1;
                                doStop();
                            } else if (mustPause) {
                                state.set(State.PAUSED);
                            }else {
                                if (pos > -1) {
                                    audioStreamToPlay = getCurrentStreamFromPlayList();
                                    for (int i=0;i<pos;i++) {
                                        bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
                                    }
                                    bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
                                    pos = -1;
                                }
                                dataLine.write(buffer, 0, bytes);
                                bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
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
                                doStop();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            },this.getClass().getName()+".Tplayer"); // The name of the Thread ends with Tplayer.

            playerThread.start();
        }
    }


    private AudioInputStream getNextStreamFromPlayList() throws IOException {
        log.debug("Player {} - getNextStreamFromPlayList : playlist count={} playlist index=",this,playList.getSize(),playList.getIndex());
        File file = playList.getNextAudioFile();
        if (file != null) {
            log.debug("Picking from play list : file {}.",file.getAbsolutePath());
            return getAudioInputStream(file);
        } else {
            log.debug("Nothing picked from playlist.");
            return null;
        }
    }

    private AudioInputStream getCurrentStreamFromPlayList() throws IOException {
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
                    listener.onBegin(index,currentFile);
                    break;
                case END:
                    listener.onEnd(index,currentFile);
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


    /**
     *
     */
    private void stopLine() {

        if (dataLine != null) {
            System.out.println("Play.playAudioStream draining line.");
            // Continues data line I/O until its buffer is drained.
            dataLine.drain();

            System.out.println("Play.playAudioStream closing line.");
            // Closes the data line, freeing any resources such as the audio device.
            dataLine.close();
        }
    }


    public void setGain(float gain) {

    }


    public void close() {
        // TODO Auto-generated method stub

    }
} // Play
