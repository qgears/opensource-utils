package hu.qgears.opengl.commons.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector4f;

/**
 * Helper class for {@link UtilGLTest#testToColor4f()}. Reads test parameters
 * (inputs and expected results) from a CSV file, and provides interface to
 * access them.
 * 
 * 
 * @author agostoni
 * 
 */
public class ToColor4fTestData {

	/**
	 * The lines of input CSV file starting with this character will be skipped
	 */
	private static final String LINE_COMMENT_MARKER = "#";
	private Map<Integer, Vector4f> testCases = new LinkedHashMap<Integer, Vector4f>();
	
	/**
	 * Returns the map containing test cases and expected results for
	 * {@link UtilGLTest#testToColor4f()} method (map is insertion-ordered).
	 * 
	 * @return
	 */
	public Map<Integer, Vector4f> getTestCases() {
		return testCases;
	}
	
	/**
	 * Loads test cases from specified csv file.
	 * 
	 * @param res the URL pointing to input file
	 * @throws IOException 
	 */
	public void initFromFileStream(URL res) throws IOException{
		InputStream in = res.openStream();
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			while(r.ready()){
				String line = r.readLine();
				parseTestCase(line);
			}
		} finally {
			in.close();
		}
	}

	private void parseTestCase(String line) {
		//skipping empty lines and comments
		if (!line.isEmpty() && !line.startsWith(LINE_COMMENT_MARKER)){
			String[] parts = line.split(" ");
			if (parts.length != 5){
				throw new RuntimeException("Invalid line in input file (exactly 5 space separated number is expected): "+line);
			}
			testCases.put(
					(int) (Long.parseLong(parts[0],16)),
					new Vector4f(
							Float.parseFloat(parts[1]),
							Float.parseFloat(parts[2]),
							Float.parseFloat(parts[3]),
							Float.parseFloat(parts[4])));
		}
	}
	
	
	/**
	 * Utility to generate random test cases.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Random r = new Random();
		int max = 255;
		int x,y,z,w;
		x = y = z = w = 0;
		for (int i = 0; i< 100 ;i++){
			x =r.nextInt(max);
			y = r.nextInt(max);
			z = r.nextInt(max);
			w = r.nextInt(max);
			String s = toHexString(x)+toHexString(y)+toHexString(z)+toHexString(w)+toFloatString(x)+toFloatString(y)+toFloatString(z)+toFloatString(w);
			System.out.println(s);
		}
		
		
	}

	private static String toHexString(int x) {
		String s = Integer.toHexString(x);
		if (s.length() == 1){
			s = "0"+s;
		}
		return s;
	}

	private static String toFloatString(int x) {
		return String.format(" %7f", x/255f);
	}
	
}
