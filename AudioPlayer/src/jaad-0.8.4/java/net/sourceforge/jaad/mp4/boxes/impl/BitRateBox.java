package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public class BitRateBox extends BoxImpl {

	private long decodingBufferSize, maxBitrate, avgBitrate;

	public BitRateBox() {
		super("Bitrate Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		decodingBufferSize = in.readBytes(4);
		maxBitrate = in.readBytes(4);
		avgBitrate = in.readBytes(4);
	}

	/**
	 * Gives the size of the decoding buffer for the elementary stream in bytes.
	 * @return the decoding buffer size
	 */
	public long getDecodingBufferSize() {
		return decodingBufferSize;
	}

	/**
	 * Gives the maximum rate in bits/second over any window of one second.
	 * @return the maximum bitrate
	 */
	public long getMaximumBitrate() {
		return maxBitrate;
	}

	/**
	 * Gives the average rate in bits/second over the entire presentation.
	 * @return the average bitrate
	 */
	public long getAverageBitrate() {
		return avgBitrate;
	}
}
