package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.od.ESDescriptor;
import net.sourceforge.jaad.mp4.od.ObjectDescriptor;

/**
 * The entry sample descriptor (ESD) box is a container for entry descriptors.
 * If used, it is located in a sample entry. Instead of an <code>ESDBox</code> a
 * <code>CodecSpecificBox</code> may be present.
 * 
 * @author in-somnia
 */
public class ESDBox extends FullBox {

	private ESDescriptor esd;

	public ESDBox() {
		super("ESD Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		
		esd = (ESDescriptor) ObjectDescriptor.createDescriptor(in);
	}

	public ESDescriptor getEntryDescriptor() {
		return esd;
	}
}
