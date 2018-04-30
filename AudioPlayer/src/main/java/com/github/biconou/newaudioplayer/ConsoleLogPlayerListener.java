package com.github.biconou.newaudioplayer;

/*-
 * #%L
 * newaudioplayer
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

import com.github.biconou.newaudioplayer.api.PlayerListener;

import java.io.File;

public class ConsoleLogPlayerListener implements PlayerListener {

    @Override
    public void onBegin(int index, File currentFile) {
        System.out.println("BEGIN : "+index+" : "+currentFile.getAbsolutePath());
    }

    @Override
    public void onEnd(int index, File currentFile) {
        System.out.println("END : "+index+" : "+currentFile.getAbsolutePath());
    }

    @Override
    public void onFinished() {
        System.out.println("FINISH");
    }

    @Override
    public void onStop() {
        System.out.println("STOP");
    }

    @Override
    public void onPause() {
        System.out.println("PAUSE");
    }
}
