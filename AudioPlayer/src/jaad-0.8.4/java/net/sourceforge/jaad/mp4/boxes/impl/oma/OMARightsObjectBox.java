package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The rights object box may be used to insert a Protected Rights Object, 
 * defined in 'OMA DRM v2.1' section 5.3.9, into a DCF or PDCF. A Mutable DRM 
 * Information box may include zero or more Rights Object boxes.
 * 
 * @author in-somnia
 */
public class OMARightsObjectBox extends FullBox {

	private byte[] data;

	public OMARightsObjectBox() {
		super("OMA DRM Rights Object Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		data = new byte[(int) getLeft(in)];
		in.readBytes(data);
	}

	/**
	 * Returns an array containing the rights object.
	 * 
	 * @return a rights object
	 */
	public byte[] getData() {
		return data;
	}
}
