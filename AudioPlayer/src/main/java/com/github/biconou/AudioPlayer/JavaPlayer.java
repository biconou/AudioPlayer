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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class JavaPlayer implements Player {


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

        AudioInputStream convertedAudioInputStream = null;

        if (audioInputStream != null) {
            AudioFormat audioFormat = audioInputStream.getFormat();
            // Convert compressed audio data to uncompressed PCM format.
            if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {

                int targetSampleSizeInBits = audioFormat.getSampleSizeInBits();
                if (targetSampleSizeInBits == -1) {
                    targetSampleSizeInBits = 16;
                }


                int targetFrameSize = 4;
                if (targetSampleSizeInBits == 24) {
                    targetFrameSize = 6;
                }

                AudioFormat convertedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        audioFormat.getSampleRate(),
                        targetSampleSizeInBits,
                        audioFormat.getChannels(),
                        targetFrameSize,
                        audioFormat.getSampleRate(),
                        false);

                convertedAudioInputStream = AudioSystem.getAudioInputStream(convertedFormat, audioInputStream);
            }
        }
        if (convertedAudioInputStream != null) {
            return convertedAudioInputStream;
        } else {
            return audioInputStream;
        }
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

                                AudioFormat PCM_44100_16 = new AudioFormat(
                                        AudioFormat.Encoding.PCM_SIGNED,
                                        (float)44100,
                                        16,
                                        audioFormat.getChannels(),
                                        4,
                                        (float)44100,
                                        false);

                                dataLine.open(PCM_44100_16);
                                audioStreamToPlay = AudioSystem.getAudioInputStream(PCM_44100_16,audioStreamToPlay);
                                audioFormat = PCM_44100_16;
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
