package com.github.biconou.AudioPlayer.audiostreams.ffmpeg;

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

import com.github.biconou.AudioPlayer.config.Configuration;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegUtils {

    private static Logger log = LoggerFactory.getLogger(FFmpegUtils.class);


    private static FFprobe ffprobe;
    static {
        try {
            ffprobe = new FFprobe(Configuration.ffprobePath);
            String ffprobeVersion = ffprobe.version();
            log.info("ffprope version {} detected",ffprobeVersion);
        } catch (Exception e) {
            ffprobe = null;
        }
    }


    private static FFmpeg ffmpeg;
    static {
        try {
            ffmpeg = new FFmpeg(Configuration.ffmpegPath);
            String ffmpegVersion = ffmpeg.version();
            log.info("ffmpeg version {} detected",ffmpegVersion);
        } catch (Exception e) {
            ffmpeg = null;
        }
    }

    static FFprobe resolveFFprobe() throws Exception {
        if (ffprobe == null) {
            throw new Exception("ffprobe is not configured");
        } else {
            return ffprobe;
        }
    }

    static FFmpeg resolveFFmpeg() throws Exception  {
        if (ffmpeg == null) {
            throw new Exception("ffmpeg is not configured");
        } else {
            return ffmpeg;
        }
    }

}
