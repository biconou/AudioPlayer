package com.github.biconou.AudioPlayer;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BufferedMultipleAudioInputStream {

	private static Logger LOG = LoggerFactory.getLogger(BufferedMultipleAudioInputStream.class);
	
	private static final int MAX_BUFFERS = 30;

	private static class Buffer {
		int nbBytes;
		byte[] bytes;
	}

	private static final Buffer lastBuffer = new Buffer();
	private static final Buffer nextStreamBuffer = new Buffer();

	private AudioInputStream currentStream = null;
	private PlayList playList = null;
	private int bufferSize;
	private BufferedMultipleAudioInputStream.Buffer[] buffers = null;

	private Thread fillThread = null;

	Boolean mustStop = Boolean.FALSE;


	private int idxForRead = -1;
	private int buffersCount = 0;



	/**
	 * 
	 * @param fromStream
	 * @return
	 */
	private AudioInputStream convertAudioInputStream(AudioInputStream fromStream) {
		AudioFormat audioFormat = fromStream.getFormat();
		// Convert compressed audio data to uncompressed PCM format.
		if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
			// if ((audioFormat.getEncoding() != AudioFormat.Encoding.PCM) ||
			//     (audioFormat.getEncoding() == AudioFormat.Encoding.ALAW) || 
			//     (audioFormat.getEncoding() == AudioFormat.Encoding.MP3)) {

			int targetSampleSizeInBits = audioFormat.getSampleSizeInBits();
			if (targetSampleSizeInBits == -1) {
				targetSampleSizeInBits = 16;
			}


			int targetFrameSize = 4;
			if (targetSampleSizeInBits == 24) {
				targetFrameSize = 6;
			}

			AudioFormat newFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED, 
					audioFormat.getSampleRate(),
					targetSampleSizeInBits,
					audioFormat.getChannels(),
					targetFrameSize,
					audioFormat.getSampleRate(),
					false);

			LOG.info("Converting audio format to {} ",newFormat);

			return  AudioSystem.getAudioInputStream(newFormat, fromStream);

		} else {
			return fromStream;
		}
	}


	/**
	 * 
	 * @return
	 */
	private void nextAudioInputStream() {

		AudioInputStream next = playList.getNextAudioStream();

		if (next == null) {
			currentStream = null;
		} else {
			currentStream = convertAudioInputStream(next);
		}
	}

	/**
	 * 
	 * @return
	 */
	AudioInputStream getCurrentStream() {
		return currentStream;
	}

	/**
	 * 
	 * @param stream
	 * @throws IOException 
	 */
	public BufferedMultipleAudioInputStream(PlayList playList,int bufferSize) throws Exception {
		
		

		this.playList = playList;
		this.bufferSize = bufferSize;

		buffers = new Buffer[MAX_BUFFERS];

		currentStream = convertAudioInputStream(playList.getFirstAudioStream());

		LOG.debug("Initial fill of buffers");
		boolean goOn = true;
		int i = 0;
		try {
			while (goOn) {
				i++;
				Buffer b = loadOneBuffer();
				if (i<MAX_BUFFERS && b != lastBuffer) {
					goOn = true;
				} else {
					goOn = false;
				}
			}				
		} catch (IOException e) {
			throw new Exception("Error while loading audio data in buffers",e);
		}

		LOG.debug("Start buffers fill thread");
		fillThread = new Thread(new streamReader());
		//fillThread.setPriority(Thread.MAX_PRIORITY);
		fillThread.setName("biconouAudioPlayer.fillThread");
		fillThread.start();
	}

	/**
	 * 
	 */

	int getBufferSize() {
		return bufferSize;
	}

	/**
	 * 
	 * @author remi
	 *
	 */
	private class streamReader implements Runnable {

		public void run() {

			boolean goOn = true;
			try {
				while (goOn) {

					synchronized (mustStop) {
						if (mustStop.equals(Boolean.TRUE)) {
							buffers = null;
							break;
						} else {
							Buffer b = loadOneBuffer();
							goOn = b != lastBuffer;							
						}
					}

				}				
			} catch (IOException e) {
				throw new RuntimeException("Error while loading audio data in buffers",e);
			}

		}

	}


	/**
	 * 
	 * @return
	 */
	public synchronized int read(byte[] returnedBytes) {
		
		int result = 0;
		
		if (buffersCount > 0) {
			Buffer removedBuffer = null;

			//System.out.println("Read at : "+idxForRead);
			removedBuffer = buffers[idxForRead];

			LOG.trace("Read : at {}",idxForRead);
			
			if (removedBuffer == lastBuffer) {
				result = -1;
			} else if (removedBuffer == nextStreamBuffer) {
				result = -2;
			} else {
				result = removedBuffer.nbBytes;
				System.arraycopy(removedBuffer.bytes,0, returnedBytes, 0, removedBuffer.nbBytes);
			}

			buffersCount --;
			idxForRead ++;
			if (idxForRead == MAX_BUFFERS) {
				if (buffersCount > 0) {
					idxForRead = 0;
				} else {
					idxForRead = -1;
				}
			}
			
			return result;
			
		} else {
			LOG.trace("Read : no buffer");
			return 0;
		}
	}

	/**
	 * 
	 */
	public void stopFillBuffers() {
		synchronized (mustStop) {
			mustStop = Boolean.TRUE;
		}
		fillThread.interrupt();
		fillThread = null;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private synchronized Buffer loadOneBuffer() throws IOException {

		if (buffersCount < MAX_BUFFERS) {

			int idxForWrite;
			if (buffersCount == 0) {
				idxForWrite = 0;
				idxForRead = 0;
			} else {
				idxForWrite = idxForRead + buffersCount;
				if (idxForWrite >= MAX_BUFFERS) {
					idxForWrite = idxForWrite - MAX_BUFFERS;
				}
			}

			LOG.trace("load : buffersCount {} : at {}",buffersCount,idxForWrite);

			if (getCurrentStream() == null) {
				buffers[idxForWrite] = lastBuffer;
			} else {
				if (buffers[idxForWrite] == null) {
					buffers[idxForWrite] = new Buffer();
					buffers[idxForWrite].bytes = new byte[getBufferSize()];
				}
				
				
				int nBytesRead = getCurrentStream().read(buffers[idxForWrite].bytes, 0, getBufferSize());
				buffers[idxForWrite].nbBytes = nBytesRead;

				if (nBytesRead == -1) {
					nextAudioInputStream();
					if (getCurrentStream() != null) {
						buffers[idxForWrite] = nextStreamBuffer;
					} else {
						return null;
					}
				}
			}

			Buffer result = buffers[idxForWrite];

			buffersCount ++;

			return result;

		} else {
			return null;
		}
	}

}
