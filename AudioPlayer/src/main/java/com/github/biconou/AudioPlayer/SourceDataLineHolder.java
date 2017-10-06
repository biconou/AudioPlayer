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

import jdk.nashorn.internal.runtime.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;

/**
 * Created by remi on 28/03/17.
 */
public class SourceDataLineHolder {

    private static Logger log = LoggerFactory.getLogger(SourceDataLineHolder.class);

    private SourceDataLine dataLine = null;
    private float gain = 0.5f;

    public SourceDataLineHolder(SourceDataLine dataLine,float gain) {
        this.dataLine = dataLine;
        this.gain = gain;
    }

    public void close() {
        if (dataLine != null) {
            log.debug("Closing dataline {}",dataLine.toString());
            try {
                dataLine.close();
            } catch (Exception e) {
                // nothing to do.
            }
        }
    }

    public static SourceDataLineHolder open(Mixer mixer, AudioInputStream audioInputStream, float gain) throws LineUnavailableException {

        SourceDataLineHolder dataLineHolder = null;
        AudioFormat audioFormat = audioInputStream.getFormat();

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            SourceDataLine dataLine;
            dataLine = (SourceDataLine) mixer.getLine(info);
            log.debug("A new line {} has been picked.", dataLine);
            dataLineHolder = new SourceDataLineHolder(dataLine, gain);

            dataLine.open(audioFormat);
            log.debug("dataline opened");

            // Allows the line to move data in and out to a port.
            dataLine.start();
            dataLineHolder.setGain(gain);
            log.debug("dataline started");
        } catch (LineUnavailableException e) {
            if (dataLineHolder != null) {
                dataLineHolder.close();
            }
            throw e;
        }
        return dataLineHolder;
    }

    private void applyGain() {
        if (gain != -1) {
            if ((gain < 0) || (gain > 1)) {
                throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
            }
        }

        if (dataLine == null) {
            return;
        }

        if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            dataLine.getControls();
            FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
            if (gain == -1) {
                control.setValue(0);
            } else {
                float max = control.getMaximum();
                float min = control.getMinimum(); // negative values all seem to be zero?
                float range = max - min;
                float masterGainLevel = min + (range * gain);

                control.setValue(min + (range * gain));
                log.debug("Master gain level is now set to {}",masterGainLevel);
            }
        } else {
            log.warn("MASTER_GAIN control not available for this line. Volume remain unchanged.");
        }
    }


    public void setGain(float gain) {
        this.gain = gain;
        applyGain();
    }

    public float getGain() {
        return this.gain;
    }

    public SourceDataLine getDataLine() {
        return dataLine;
    }
}
