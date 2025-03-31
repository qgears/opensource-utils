package hu.qgears.opengl.commons.test;

import static org.junit.Assert.assertEquals;
import hu.qgears.opengl.commons.UtilGl;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;

import org.junit.Test;
import org.lwjgl.util.vector.Vector4f;

/**
 * Unit tests for {@link UtilGl} class.
 * 
 * @author agostoni
 * 
 */
public class UtilGLTest {

	/**
	 * This delta is used when comparing float color vectors (two float value
	 * are treated as equal if theirs difference is less then this constant).
	 */
	private static final double COLOR_CHANNEL_DELTA = 1.d / 255 / 10;

	/**
	 * Testing color bitfield conversion functionality
	 * {@link UtilGl#toColor4f(int)}
	 * 
	 * @throws IOException
	 */
	@Test
	public void testToColor4f() throws IOException{
		ToColor4fTestData data = new ToColor4fTestData();
		URL res = loadResource("res/toColor4ftests.csv");
		data.initFromFileStream(res);
		for (Entry<Integer, Vector4f> es : data.getTestCases().entrySet()){
			int input = es.getKey(); 
			Vector4f result = UtilGl.toColor4f(input);
			assertVectorEqual("Testing "+Integer.toHexString(input) + " failed!",es.getValue(),result);
		}
	}

	/**
	 * Compares two float vectors and raises assertion error if coordinates
	 * differ. The {@link #COLOR_CHANNEL_DELTA} constant is used as delta when
	 * comparing two float coordinates.
	 * 
	 * @param message Custom error message which will be included in assertion error
	 * @param expected The expected vector
	 * @param actual The actual vector
	 */
	private void assertVectorEqual(String message, Vector4f expected, Vector4f actual) {
		assertEquals(message +" X coordinate of vectors differ!", expected.getX(), actual.getX(), COLOR_CHANNEL_DELTA);
		assertEquals(message + " Y coordinate of vectors differ!", expected.getY(), actual.getY(), COLOR_CHANNEL_DELTA);
		assertEquals(message + " Z coordinate of vectors differ!", expected.getZ(), actual.getZ(), COLOR_CHANNEL_DELTA);
		assertEquals(message + " W coordinate of vectors differ!", expected.getW(), actual.getW(), COLOR_CHANNEL_DELTA);
	}

	protected URL loadResource(String name) {
		URL res = getClass().getResource(name);
		if (res != null){
			return res;
		} else {
			throw new RuntimeException("Resource cannot be found: "+name);
		}
	}
	
}
