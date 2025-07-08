package hu.qgears.images.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import hu.qgears.images.libpng.NativeLibPngAccessor;
import hu.qgears.images.tiff.NativeTiffLoaderAccessor;
import hu.qgears.nativeloader.XMLNativeLoaderValidator;

public class TestNativesPackaged {

	
	@Test
	public void testLibpng()  {
		try {
			XMLNativeLoaderValidator.check(new NativeLibPngAccessor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void testTiff()  {
		try {
			XMLNativeLoaderValidator.check(new NativeTiffLoaderAccessor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
