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

import com.github.biconou.newaudioplayer.audiostreams.AudioInputStreamUtils;
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

    private static BufferHolder endBuffer = new BufferHolder();
    private BlockingQueue<BufferHolder> queue = new LinkedBlockingQueue<>(10);
    AudioFormat audioFormat = null;
    int bytesPerSecond = 0;
    AudioInputStream audioInputStream = null;
    int secondsAlreadyRead = 0;

    static class BufferHolder{
        public int bytes;
        public byte[] buffer;
    }

    public AudioBuffers(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
        this.audioFormat = this.audioInputStream.getFormat();
        this.bytesPerSecond = AudioInputStreamUtils.computeBytesPerSecond(audioInputStream);

        log.debug("Number of bytes per second : "+this.bytesPerSecond);
    }


    void fillBuffers()  {
        Thread T = new Thread(() -> {
            try {
                int bytes = 0;
                while (bytes != -1) {
                    try {
                        BufferHolder holder = new BufferHolder();
                        holder.buffer = new byte[bytesPerSecond];
                        bytes = AudioInputStreamUtils.readOneSecond(audioInputStream,holder.buffer,bytesPerSecond);
                        if (bytes != -1) {
                            holder.bytes = bytes;
                            queue.put(holder);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                queue.put(endBuffer);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        T.start();
    }

    BufferHolder getOneSecondOfMusic() {
        BufferHolder polled;
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


    public void skip(int seconds) throws IllegalStateException {
        if (seconds < secondsAlreadyRead) {
            throw new IllegalStateException("Try to skip to illegal position "+seconds+" as secondsAlreadyRead is "+secondsAlreadyRead);
        } else {
            while (secondsAlreadyRead < seconds) {
                getOneSecondOfMusic();
            }
        }
    }
}
