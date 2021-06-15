package hu.qgears.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class UtilFile {

	public static String loadFileAsString(URL resource) throws IOException {
		StringBuilder ret = new StringBuilder();
		InputStream is = resource.openStream();
		try {
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			int ch;
			while ((ch = isr.read()) >= 0) {
				ret.append((char) ch);
			}
		} finally {
			is.close();
		}
		return ret.toString();
	}

}
