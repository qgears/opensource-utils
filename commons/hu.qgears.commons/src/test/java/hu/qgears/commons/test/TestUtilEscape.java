package hu.qgears.commons.test;

import hu.qgears.commons.UtilEscape;

import org.junit.Assert;
import org.junit.Test;


public class TestUtilEscape {
	static String src="\\\n\r\'\"";
	static String src_c="\\\n\r\'\"\t?";
	@Test
	public void test()
	{
		String s="\\\n\r\'\"";
		Assert.assertEquals(src, s);
		String out=UtilEscape.escapeToJavaString(src);
		String ref="\\\\\\n\\r\\\'\\\"";
		Assert.assertEquals(ref, out); 
	}
	
	@Test
	public void testANSIC()
	{
		String s="\\\n\r\'\"\t?";
		Assert.assertEquals(src_c, s);
		String out=UtilEscape.escapeToANSICString(src_c);
		String ref="\\\\\\n\\r\\\'\\\"\\t\\?";
		Assert.assertEquals(ref, out); 
	}
}
