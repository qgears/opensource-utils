package hu.qgears.commons.test;

import hu.qgears.commons.UtilEscape;

import org.junit.Assert;
import org.junit.Test;


public class TestUtilEscape {
	static String src="\\\n\r\'\"";
	@Test
	public void test()
	{
		String s="\\\n\r\'\"";
		Assert.assertEquals(src, s);
		String out=UtilEscape.escapeToJavaString(src);
		String ref="\\\\\\n\\r\\\'\\\"";
		Assert.assertEquals(ref, out); 
	}
}
