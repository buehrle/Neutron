package com.erdlof.neutron.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
	public static <T extends OutputStream, E extends InputStream> void copyStream(E in, T out) throws IOException {
		byte[] buffer = new byte[1024];
		
		int len = in.read(buffer);
		
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		
		out.flush();
	}
}
