package hu.qgears.shm.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import hu.qgears.nativeloader.XMLNativeLoaderValidator;
import hu.qgears.shm.natives.Accessor;

public class TestShmNativesPackaged {
	@Test
	public void testShm()  {
		try {
			XMLNativeLoaderValidator.check( new Accessor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
