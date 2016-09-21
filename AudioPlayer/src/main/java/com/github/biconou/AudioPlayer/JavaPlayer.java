package com.github.biconou.AudioPlayer;


import com.sun.media.sound.WaveExtensibleFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;

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
    public static Map<String,AudioFormat> supportedFormats = new HashMap<>();

    static {
        allFormats.add(PCM_SIGNED_44100_16_LE);
        allFormats.add(PCM_SIGNED_96000_24_LE);

        allFormats.forEach(format -> {
            System.out.println(format.toString());
            SourceDataLine dataLine = null;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            } else {
                try {
                    dataLine = (SourceDataLine) AudioSystem.getLine(info);
                    dataLine.open(format);
                    dataLine.close();
                    supportedFormats.put(format.toString(),format);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static boolean isFormatSupported(AudioFormat format) {
        final boolean[] isSupported = {false};
        supportedFormats.keySet().forEach(s -> {if (format.toString().equals(s)) isSupported[0] = true;});
        return isSupported[0];
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

    public static AudioInputStream getAudioInputStream(File file) {
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
        int sampleSizeInBits = audioFormat.getSampleSizeInBits();
        float sampleRate = audioFormat.getSampleRate();

        AudioFormat targetFormat = PCM_SIGNED_44100_16_LE;

        if (sampleSizeInBits == 24 && sampleRate == (float) 96000) {
            if (isFormatSupported(PCM_SIGNED_96000_24_LE)) {
                targetFormat = PCM_SIGNED_96000_24_LE;
            }
        }

        return AudioSystem.getAudioInputStream(targetFormat, audioInputStream);

    }

    private final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);

    PlayList playList = null;
    SourceDataLine dataLine = null;
    List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    Boolean mustPause = Boolean.FALSE;
    Boolean mustStop = Boolean.FALSE;
    AudioFormat previousUsedAudioFormat = null;


    public State getState() {
        return state.get();
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    public void registerListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public void stop() {
        this.mustStop = Boolean.TRUE;
    }

    public void pause() {
        mustPause = Boolean.TRUE;
    }

    public void play() throws NothingToPlayException {

        if (getState().equals(State.PAUSED)) {
            mustPause = Boolean.FALSE;
        } else {

            if (playList == null) {
                throw new NothingToPlayException();
            }

            Thread playerThread = new Thread(() -> {
                AudioInputStream nextAudioStream = null;
                try {
                    nextAudioStream = getNextStreamFromPlayList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                while (nextAudioStream != null) {

                    notifyNextStream(nextAudioStream);

                    state.set(State.PLAYING);

                    AudioInputStream audioStreamToPlay = nextAudioStream;

                    // Audio format provides information like sample rate, size, channels.
                    AudioFormat audioFormat = nextAudioStream.getFormat();


                    try {
                        if (previousUsedAudioFormat == null || !audioFormat.toString().equals(previousUsedAudioFormat.toString())) {
                            previousUsedAudioFormat = audioFormat;
                            if (dataLine != null) {
                                dataLine.close();
                            }

                            // Open a data line to play our type of sampled audio.
                            // Use SourceDataLine for play and TargetDataLine for record.

                            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                            if (!AudioSystem.isLineSupported(info)) {
                                System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
                                return;
                            }

                            // Create a SourceDataLine for play back (throws LineUnavailableException).
                            dataLine = (SourceDataLine) AudioSystem.getLine(info);
                            // System.out.println("SourceDataLine class=" + dataLine.getClass());

                            // The line acquires system resources (throws LineAvailableException).
                            try {
                                dataLine.open(audioFormat);
                            } catch (LineUnavailableException e) {
                                throw new RuntimeException(e);
                            }

                            // Allows the line to move data in and out to a port.
                            dataLine.start();
                        }

                        // Create a buffer for moving data from the audio stream to the line.
                        final int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
                        byte[] buffer = new byte[bufferSize];
                        int bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
                        while (bytes != -1) {
                            if (mustStop) {
                                stopLine();
                                notifyEnd();
                                state.set(State.CLOSED);
                            } else if (mustPause) {
                                // do nothing and continue loop
                            }else {
                                dataLine.write(buffer, 0, bytes);
                                bytes = audioStreamToPlay.read(buffer, 0, bufferSize);
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (state.get() == State.CLOSED) {
                        nextAudioStream = null;
                    } else {
                        try {
                            nextAudioStream = getNextStreamFromPlayList();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            },this.getClass().getName()+".Tplayer");

            playerThread.start();
        }
    }

    private AudioInputStream getNextStreamFromPlayList() throws IOException {
        File file = playList.getNextAudioFile();
        if (file != null) {
            return getAudioInputStream(file);
        } else {
            return null;
        }
    }

    private void notifyNextStream(AudioInputStream nextAudioStream) {
        for (PlayerListener listener : listeners) {
            listener.nextStreamNotified(nextAudioStream);
        }
    }

    private void notifyEnd() {
        for (PlayerListener listener : listeners) {
            listener.endNotified();
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
