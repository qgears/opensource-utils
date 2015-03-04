package hu.qgears.commons.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilString;

import org.junit.Test;

/**
 * Test methods of {@link UtilString}
 * 
 * @author adam
 *
 */
public class TestUtilString {

	private static final String HELLO_WORLD = "HelloWorld";

	@Test
	public void testCharacterStrip(){
		List<Integer> codePoints = new ArrayList<Integer>();
		codePoints.add(0x000A);
		codePoints.add(0x007F);
		codePoints.add(0x0080);
		codePoints.add(0x2028);
		for(int cp : codePoints){
			char[] chars = Character.toChars(cp);
			String stripped = UtilString.stripBlacklistedCharacters("Hello"+new String(chars)+ "World");
			assertEquals("UtilString didn't strip character: \""+String.valueOf(chars)+"\".",HELLO_WORLD, stripped);
		}
	}
	@Test
	public void testStripIgnore(){
		String input = "Hello World!";
		String stripped = UtilString.stripBlacklistedCharacters(input);
		assertEquals("UtilString unexpectedly stripped characters from input",input,stripped);
	}
	
}
