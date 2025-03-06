package hu.qgears.parser.test.tokenizer;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.parser.tokenizer.impl.TextSource;

public class TextSourceTest {
	@Test
	public void firstCharsTest() {
		TextSource t = new TextSource("kiscica".toCharArray());
		Assert.assertEquals("kis", t.firstChars(3));
		Assert.assertEquals("isc", t.firstChars(1, 3));
		Assert.assertEquals("ca", t.firstChars(5, 10));
	}
	
	@Test
	public void getCharAtTest() {
		char[] array = "kiscica".toCharArray();
		Assert.assertEquals("k", TextSource.getCharAt(0, array, 0).toString());
		Assert.assertEquals("s", TextSource.getCharAt(1, array, 1).toString());
	}
	
	@Test
	public void startsWithTest() {
		char[] array = "kiscica".toCharArray();
		Assert.assertEquals(true, TextSource.startsWith(array, 0, "kisc".toCharArray()));
		Assert.assertEquals(false, TextSource.startsWith(array, 2, "kisc".toCharArray()));
		Assert.assertEquals(true, TextSource.startsWith(array, 2, "scic".toCharArray()));
	}
	
	@Test
	public void lastCharsTest() {
		TextSource t = new TextSource("kiscica".toCharArray());
		Assert.assertEquals("is", t.lastChars(3, 2));
		Assert.assertEquals("kis", t.lastChars(3, 10));
		Assert.assertEquals("ki", t.lastChars(2, 3));
	}
	
	@Test
	public void substringTest() {
		TextSource t = new TextSource("kiscica".toCharArray());
		Assert.assertEquals("k", t.substring(0, 1));
		Assert.assertEquals("ica", t.substring(4, 10));
		Assert.assertEquals("scic", t.substring(2, 6));

	}
}
