package hu.qgears.images.test;

import hu.qgears.images.text.RGBAColor;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test color parsing methods.
 * @author rizsi
 *
 */
public class TestColor {
	private static final RGBAColor DEF_C = RGBAColor.WHITE;
	@Test
	public void testParseColor()
	{
		Assert.assertEquals(new RGBAColor(1,2,3), RGBAColor.fromCssNotation("rgb(1,2,3)",DEF_C));
		Assert.assertEquals(new RGBAColor(1,2,3), RGBAColor.fromCssNotation("rgba(1,2,3,1)",DEF_C));
		Assert.assertEquals(new RGBAColor(1,2,3), RGBAColor.fromCssNotation("rgba(1,2,3,1.0)",DEF_C));
		Assert.assertEquals(new RGBAColor(1,2,3, 255), RGBAColor.fromCssNotation("rgba(1,2,3,1.0)",DEF_C));
		Assert.assertEquals(new RGBAColor(1,2,3, 55), RGBAColor.fromCssNotation("rgba(1, 2, 3, "+55.0/255+")",DEF_C));
		Assert.assertEquals(new RGBAColor(1,2,3), RGBAColor.fromCssNotation("#010203",DEF_C));
		Assert.assertEquals(new RGBAColor(255,255,255), RGBAColor.fromCssNotation("#ffffff",DEF_C));
		Assert.assertEquals(new RGBAColor(255,255,255), RGBAColor.fromCssNotation("#ffFfff",DEF_C));
		Assert.assertFalse(new RGBAColor(0,0,0,0).equals(new RGBAColor(1,0,0,0)));
		Assert.assertFalse(new RGBAColor(0,0,0,0).equals(new RGBAColor(0,1,0,0)));
		Assert.assertFalse(new RGBAColor(0,0,0,0).equals(new RGBAColor(0,0,1,0)));
		Assert.assertFalse(new RGBAColor(0,0,0,0).equals(new RGBAColor(0,0,0,1)));
		Assert.assertEquals(new RGBAColor(255,255,255), RGBAColor.fromCssNotation("not a proper color string",DEF_C));
		Assert.assertEquals(new RGBAColor(2,6,3,42), RGBAColor.fromCssNotation("not a proper color string",
				new RGBAColor(2,6,3,42)));
		RGBAColor c=new RGBAColor(5,6,7,8);
		Assert.assertEquals(5, c.r);
		Assert.assertEquals(6, c.g);
		Assert.assertEquals(7, c.b);
		Assert.assertEquals(8, c.a);
		Assert.assertEquals(new RGBAColor(5,6,7,9), c.newWithAlpha(9));
	}
	@Test
	public void testParseToString()
	{
		testParseToString("rgb(0,0,0,0)");
		testParseToString("rgb(0,0,0,1.0)");
		testParseToString("rgb(0,0,0,.13)");
		testParseToString("rgb(41,42,43,.13)");
		testParseToString("rgb(41,42,43,1.0)");
		testParseToString("rgb(255,255,255,1.0)");
		Assert.assertEquals("rgb(255,255,255)", new RGBAColor(255,255,255,255).toString());
		Assert.assertEquals("rgba(255,255,255,0.5019608)", new RGBAColor(255,255,255,128).toString());
		Assert.assertEquals("rgba(45,46,47,0.0)", new RGBAColor(45,46,47,0).toString());
		for(int i=0;i<256;++i)
		{
			Assert.assertEquals(new RGBAColor(45,46,47,i),
					RGBAColor.fromCssNotation(new RGBAColor(45,46,47,i).toCssParameter(), DEF_C));
		}
	}
	private void testParseToString(String cssNotation) {
		RGBAColor c=RGBAColor.fromCssNotation(cssNotation,DEF_C);
		String toString=c.toString();
		Assert.assertEquals(c, RGBAColor.fromCssNotation(toString,null));
	}
	/**
	 * Test hash code implementation of {@link RGBAColor}
	 */
	@Test
	public void testHash()
	{
		Assert.assertEquals(new RGBAColor(0,0,0,0).hashCode(), new RGBAColor(0,0,0,0).hashCode());
		Assert.assertEquals(new RGBAColor(1,0,0,0).hashCode(), new RGBAColor(1,0,0,0).hashCode());
		Assert.assertEquals(new RGBAColor(0,1,0,0).hashCode(), new RGBAColor(0,1,0,0).hashCode());
		Assert.assertEquals(new RGBAColor(0,0,1,0).hashCode(), new RGBAColor(0,0,1,0).hashCode());
		Assert.assertEquals(new RGBAColor(0,0,0,1).hashCode(), new RGBAColor(0,0,0,1).hashCode());
		Assert.assertFalse(new RGBAColor(0,0,0,0).hashCode()==new RGBAColor(1,0,0,0).hashCode());
		Assert.assertFalse(new RGBAColor(0,0,0,0).hashCode()==new RGBAColor(0,1,0,0).hashCode());
		Assert.assertFalse(new RGBAColor(0,0,0,0).hashCode()==new RGBAColor(0,0,1,0).hashCode());
		Assert.assertFalse(new RGBAColor(0,0,0,0).hashCode()==new RGBAColor(0,0,0,1).hashCode());
	}
}

