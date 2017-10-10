package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The item protection box provides an array of item protection information, for
 * use by the Item Information Box.
 *
 * @author in-somnia
 */
public class ItemProtectionBox extends FullBox {

	public ItemProtectionBox() {
		super("Item Protection Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int protectionCount = (int) in.readBytes(2);

		readChildren(in, protectionCount);
	}
}
