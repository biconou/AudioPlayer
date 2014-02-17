package com.github.biconou.cmus;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;

class CMusController {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CMusController.class);
	
	private Process cmusRemoteProcess;
	private  BufferedOutputStream cmdStream = null;
	private BufferedReader cmusIStream = null;
	private BufferedReader cmusErrorStream = null;
	Thread tErrorCMus = null;
	
	/**
	 * Public constructor.
	 * @throws Exception 
	 */
	public CMusController() throws Exception {
		
		LOG.debug("Creation of a new CMusControler");
		ProcessBuilder pb = new ProcessBuilder("cmus-remote");
		LOG.debug("Start a new process : {}",pb.command());
		try {
			cmusRemoteProcess = pb.start();
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e);
			LOG.error("Can not create a cmus-remote process",ex);
			throw ex;
		}
		LOG.debug("Opening the command pipe");
		OutputStream cmusRemoteOStream = cmusRemoteProcess.getOutputStream();
		cmdStream = new BufferedOutputStream(cmusRemoteOStream);
		
		LOG.debug("Creating a thread to read (and log) cmus-remote standard error stream");
		cmusErrorStream = new BufferedReader(new InputStreamReader(cmusRemoteProcess.getErrorStream()));
		
		tErrorCMus = new Thread(new Runnable() {

			public void run() {
				String readLine;
				try {
					readLine = cmusErrorStream.readLine();
					while (readLine != null) {	            	  
						LOG.error("CMUS_ERROR | {}",readLine);
						// read next line
						readLine = cmusErrorStream.readLine();
					}
				}
				catch (IOException e) {
					LOG.error("Error while reading cmus-remote stderr",e);
				}
			}
		});

		tErrorCMus.start();
		
		//

		LOG.debug("Set softvolume on in cmus");
		sendCommandToCMus("set softvol=true");
		LOG.debug("Set status display program in cmus");
		sendCommandToCMus("set status_display_program=/subsonic/4.8/cmus-status-display");

		

	}
	
	/**
	 * Send a command to cmus via cmus-remote.
	 * 
	 * @param command
	 * @throws Exception
	 */
	private void sendCommandToCMus(String command) throws Exception {
		LOG.debug("Sending command to cmus : {}",command);
		try {
		if (!command.endsWith("\n")) {
			command = command + "\n";
		}
		cmdStream.write(command.getBytes("UTF-8"));
		cmdStream.flush();
		} catch (Exception e) {
			LOG.error("Error sending command to cmus",e);
			throw e;
		}
	}
	
	
	/**
	 * Stop playing in cmus
	 * 
	 * @throws Exception 
	 * 
	 */
	public void stop() throws Exception {
		sendCommandToCMus("player-stop");
	}
	
	/**
	 * Pause playing in cmus
	 * 
	 * @throws Exception 
	 * 
	 */
	public void pause() throws Exception {
		sendCommandToCMus("player-pause");
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void clearPlayQueue() throws Exception {
		sendCommandToCMus("clear -q");
	}
	
	public void addFile(String file) throws Exception {
		sendCommandToCMus("add -q "+file);
	}
	
	public void next() throws Exception {
		sendCommandToCMus("player-next");
	}
	
	public void play() throws Exception {
		sendCommandToCMus("player-play");
	}
	
	/**
	 * Clear play queue and init a new play queue with the given file name ready to be played.
	 * 
	 * @param file The given file name  (mus be an absolute file path and name).
	 * @throws Exception 
	 */
	public void initPlayQueue(String file) throws Exception {
		stop();
		clearPlayQueue();
		addFile(file);
		next();
	}
	
	/**
	 * Close the cmus controler and release resources.
	 */
	public void close(){
		cmusRemoteProcess.destroy();
		if (cmdStream != null) {
			try {
				cmdStream.close();
				cmusIStream.close();
				cmusErrorStream.close();
				
				if (tErrorCMus != null) {
					tErrorCMus.interrupt();
				}
			} catch (IOException e) {
				// Nothing to do.
			}
			cmdStream = null;
		}
	}

	/**
	 * 
	 * @param gain
	 * @throws Exception 
	 */
	public void setGain(float gain) throws Exception {
		int vol = (int)(gain*100);
		sendCommandToCMus("set softvol_state="+vol+" "+vol);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String status() throws Exception {
		LOG.debug("Begin status()");
		cmusIStream = new BufferedReader(new InputStreamReader(cmusRemoteProcess.getInputStream()));
		
		String status = null;
		
		sendCommandToCMus("status");
		
		String readLine = "";
		while (readLine != null) {	            	  			
			readLine = cmusIStream.readLine();
			if (readLine.startsWith("status ")) {
				status = readLine.substring(7);
				status = status.trim();
				break;
			}
		}
		
		//cmusIStream.close();
		cmusIStream = null;
		
		if (status == null || status.trim().length() == 0) {
			Exception ex = new Exception("Status should not be null");
			LOG.error("Error while reading status",ex);
			throw ex;
		}
		
		LOG.debug("End status()");
		return status; 
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public boolean isPaused() throws Exception {
		String status = status();
		if (status.equals("paused")) {
			return true;
		}
		return false;
	}
	
}
