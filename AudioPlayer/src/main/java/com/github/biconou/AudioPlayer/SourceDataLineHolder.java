package com.github.biconou.AudioPlayer;

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
