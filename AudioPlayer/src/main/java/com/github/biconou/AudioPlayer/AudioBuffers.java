package com.github.biconou.AudioPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by remi on 05/04/17.
 */
public class AudioBuffers {

    private static Logger log = LoggerFactory.getLogger(AudioBuffers.class);

    private static byte[] endBuffer = new byte[]{};
    private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(10);
    AudioFormat audioFormat = null;
    int bytesPerSecond = 0;
    AudioInputStream audioInputStream = null;
    int secondsAlreadyRead = 0;
    boolean continueFill = true;

    public AudioBuffers(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
        this.audioFormat = this.audioInputStream.getFormat();
        this.bytesPerSecond = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();

        log.debug("Number of bytes per second : "+this.bytesPerSecond);
    }


    void fillBuffers()  {
        Thread T = new Thread(() -> {
            try {
                int bytes = 0;
                while (bytes != -1 && continueFill) {
                    byte[] buffer = new byte[bytesPerSecond];
                    try {
                        bytes = AudioSystemUtils.readOneSecond(audioInputStream,buffer,bytesPerSecond);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    queue.put(buffer);
                }
                if (!continueFill) {
                    if (audioInputStream != null) {
                        try {
                            audioInputStream.close();
                            audioInputStream = null;
                            queue.clear();
                            queue = null;
                        } catch (IOException e) {
                            // Ignore exception
                        }
                    }
                }
                queue.put(endBuffer);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        T.start();
    }

    byte[] getOneSecondOfMusic() {
        byte[] polled;
        try {
            polled = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (polled == endBuffer) {
            return null;
        } else {
            secondsAlreadyRead++;
            return polled;
        }
    }


    public void discard() {
        continueFill = false;
    }

    public void skip(int seconds) throws IllegalStateException {
        if (seconds <= secondsAlreadyRead) {
            throw new IllegalStateException();
        } else {
            while (secondsAlreadyRead < seconds) {
                getOneSecondOfMusic();
            }
        }
    }
}
