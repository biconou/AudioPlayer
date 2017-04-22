package com.github.biconou.cmus;

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


public class TestCmus {

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
