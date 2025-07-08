package hu.qgears.opengl.commons.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import hu.qgears.nativeloader.XMLNativeLoaderValidator;
import hu.qgears.opengl.glut.GlutAccessor;

public class TestOpenGlCommonsNativesPackaged {
	@Test
	public void testGlut()  {
		try {
			XMLNativeLoaderValidator.check(new GlutAccessor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
