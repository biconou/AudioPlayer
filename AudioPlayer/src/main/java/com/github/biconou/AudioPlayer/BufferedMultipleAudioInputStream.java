package com.github.biconou.AudioPlayer;

import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class BufferedMultipleAudioInputStream {

	private static final int MAX_BUFFERS = 30;

	private static class Buffer {
		int nbBytes;
		byte[] bytes;
	}
	
	private static final Buffer lastBuffer = new Buffer();
	private static final Buffer nextStreamBuffer = new Buffer();

	//private int currentStreamIndex = -1;
	private AudioInputStream currentStream = null;
	private PlayList playList = null;
	private int bufferSize;
	//List<byte[]> buffers = null;
	private Vector<BufferedMultipleAudioInputStream.Buffer> buffers = null;

	private Thread fillThread = null;
	
	Boolean mustStop = Boolean.FALSE;


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
    		
            System.out.println("Converting audio format to " + newFormat);
            
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
		//currentStreamIndex ++;
		//currentStream = null;
		
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
	public BufferedMultipleAudioInputStream(PlayList playList,int bufferSize) throws IOException {
		
		this.playList = playList;
		this.bufferSize = bufferSize;

		//buffers = new ArrayList<byte[]>();
		buffers = new Vector<BufferedMultipleAudioInputStream.Buffer>();
		
		currentStream = convertAudioInputStream(playList.getFirstAudioStream());

		//first read
		boolean goOn = true;
		int i = 0;
		try {
			while (goOn) {
				i++;
				Buffer b = loadOneBuffer();
				if (i<10 && b != lastBuffer) {
					goOn = true;
				} else {
					goOn = false;
				}
			}				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fillThread = new Thread(new streamReader());
		fillThread.setPriority(Thread.MIN_PRIORITY);
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
							buffers.clear();
							buffers = null;
							break;
						} else {
							if (buffers.size() < MAX_BUFFERS) {
								Buffer b = loadOneBuffer();
								goOn = b != lastBuffer;
							} 
						}
					}
					
				}				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}


	/**
	 * 
	 * @return
	 */
	public int read(byte[] returnedBytes) {
		if (buffers.size() > 0) {
			Buffer removedBuffer = null;
			//System.out.println("-read "+buffers.size());
			removedBuffer = buffers.remove(0);
			//System.out.println("+read "+buffers.size());
			if (removedBuffer == lastBuffer) {
				return -1;
			}
			if (removedBuffer == nextStreamBuffer) {
				return -2;
			}
			System.arraycopy(removedBuffer.bytes,0, returnedBytes, 0, removedBuffer.nbBytes);
			return removedBuffer.nbBytes;
		} else {
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
//		while (buffers != null) {
//			// Do nothing but wait until buffers is null.
//		}
		fillThread.interrupt();
		fillThread = null;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private Buffer loadOneBuffer() throws IOException {
		byte[] b = new byte[getBufferSize()];
		
		if (getCurrentStream() == null) {
			buffers.add(lastBuffer);
			return lastBuffer;			
		}
		
		int nBytesRead = getCurrentStream().read(b, 0, getBufferSize());
		if (nBytesRead == -1) {
			buffers.add(nextStreamBuffer);
			nextAudioInputStream();
			return loadOneBuffer();
		}
		Buffer newBuffer = new Buffer();
		newBuffer.bytes = b;
		newBuffer.nbBytes = nBytesRead;					
		buffers.add(newBuffer);
		//System.out.println("fill "+b.length);
		return newBuffer;
	}

}
