package hu.qgears.opengl.commons.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import hu.qgears.nativeloader.XMLNativeLoaderValidator;
import hu.qgears.opengl.osmesa.OsMesaAccessor;

public class TestOsMesaNativesPackaged {
	@Test
	public void testGlut()  {
		try {
			XMLNativeLoaderValidator.check(new OsMesaAccessor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
