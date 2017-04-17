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

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * Created by remi on 28/03/17.
 */
public class SourceDataLineHolder {

    private SourceDataLine dataLine = null;
    private float gain = 0.5f;

    public SourceDataLineHolder(SourceDataLine dataLine,float gain) {
        this.dataLine = dataLine;
        this.gain = gain;
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

        try {
            FloatControl control = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
            if (gain == -1) {
                control.setValue(0);
            } else {
                float max = control.getMaximum();
                float min = control.getMinimum(); // negative values all seem to be zero?
                float range = max - min;

                control.setValue(min + (range * gain));
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
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
