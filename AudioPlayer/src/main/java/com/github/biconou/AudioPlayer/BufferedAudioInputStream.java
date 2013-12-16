package com.github.biconou.AudioPlayer;

import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class BufferedAudioInputStream {

	private static final int MAX_BUFFERS = 30;

	private static class Buffer {
		int nbBytes;
		byte[] bytes;
	}
	
	private static final Buffer lastBuffer = new Buffer();

	private int currentStreamIndex = -1;
	private AudioInputStream currentStream = null;
	private AudioInputStream[] streams = null;
	private int bufferSize;
	//List<byte[]> buffers = null;
	private Vector<BufferedAudioInputStream.Buffer> buffers = null;

	private Thread fillThread = null;

	Integer lock = new Integer(0);

	/**
	 * 
	 * @return
	 */
	AudioInputStream getNextAudioInputStream() {
		currentStreamIndex ++;
		currentStream = null;
		
		
		AudioInputStream next = null;
		
		try {
			next = streams[currentStreamIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

		System.out.println("Next audio stream : "+currentStreamIndex);

        AudioFormat audioFormat = next.getFormat();
        // Convert compressed audio data to uncompressed PCM format.
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            // if ((audioFormat.getEncoding() != AudioFormat.Encoding.PCM) ||
            //     (audioFormat.getEncoding() == AudioFormat.Encoding.ALAW) || 
            //     (audioFormat.getEncoding() == AudioFormat.Encoding.MP3)) {
            AudioFormat newFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, 
                    audioFormat.getSampleRate(),
                    16,
                    audioFormat.getChannels(),
                    audioFormat.getChannels() * 2,
                    audioFormat.getSampleRate(),
                    false);
            System.out.println("Converting audio format to " + newFormat);
            AudioInputStream newStream = AudioSystem.getAudioInputStream(newFormat, next);
            return newStream;
        } else {
        	return next;
        }
	}
	
	/**
	 * 
	 * @return
	 */
	AudioInputStream getCurrentStream() {
		if (currentStream == null) {
			currentStream = getNextAudioInputStream();
		}
		return currentStream;
	}

	/**
	 * 
	 * @param stream
	 * @throws IOException 
	 */
	public BufferedAudioInputStream(AudioInputStream[] streams,int bufferSize) throws IOException {
		
		this.streams = streams;
		this.bufferSize = bufferSize;

		//buffers = new ArrayList<byte[]>();
		buffers = new Vector<BufferedAudioInputStream.Buffer>();

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

					//System.out.println("try fill");
					if (buffers.size() < MAX_BUFFERS) {
					Buffer b = loadOneBuffer();
					goOn = b != lastBuffer;
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
			System.arraycopy(removedBuffer.bytes,0, returnedBytes, 0, removedBuffer.nbBytes);
			return removedBuffer.nbBytes;
		} else {
			return 0;
		}
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
			currentStream = null;
			return loadOneBuffer();
		}
		Buffer newBuffer = new Buffer();
		newBuffer.bytes = b;
		newBuffer.nbBytes = nBytesRead;					
		buffers.add(newBuffer);
		//System.out.println("fill "+buffers.size());
		return newBuffer;
	}

}
