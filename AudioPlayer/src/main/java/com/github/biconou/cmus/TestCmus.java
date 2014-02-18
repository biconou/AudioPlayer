package com.github.biconou.cmus;

public class TestCmus {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			CMusController cmus = new CMusController("localhost",4041 , "subsonic");
			cmus.initPlayQueue("/mnt/NAS/REMI/Ma musique/16 Horsepower/16 horsepower ep/01 haw.mp3");
			cmus.play();
			cmus.addFile("/mnt/NAS/REMI/Ma musique/16 Horsepower/16 horsepower ep/02 south pennsylvania waltz.mp3");
			Thread.sleep(15000);
			cmus.pause();
			System.out.println("paused ?"+cmus.isPaused());
			Thread.sleep(2000);
			cmus.play();
			Thread.sleep(5000);
			cmus.next();
			System.out.println(cmus.status().getFile());
			
		} catch (Exception e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		

	}

}
