package hu.qgears.parser.test.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import hu.qgears.parser.util.LineIndex;
import hu.qgears.parser.util.LineIndex.LineInfo;
import hu.qgears.parser.util.UtilFile;

public class LineIndexTest {

	
	@Test
	public void test1() {
		
		LineIndex index = new LineIndex(null).buildIndex("Hello World");
		
		LineInfo l = index.getLine(0);
		assertEquals(l.line, 1);
		assertEquals(l.col, 1);

		l = index.getLine(4);
		assertEquals(l.line, 1);
		assertEquals(l.col, 5);
		
		
		assertEquals(4, index.getRawposition(1,5));
		
	} 

	@Test
	public void test2() {
		String t = "123456\n789";
		LineIndex index = new LineIndex(null).buildIndex(t);
		
		LineInfo l = index.getLine(t.indexOf('9'));
		assertEquals(2,l.line);
		assertEquals(3,l.col);
		
		
	} 
	@Test
	public void test3() throws IOException {
		String t =  UtilFile.loadFileAsString(LineIndexTest.class.getResource("test.txt"))  ;
		LineIndex index = new LineIndex(null).buildIndex(t);
		
		LineInfo l = index.getLine(t.indexOf('B'));
		assertEquals(14,l.line);
		assertEquals(6,l.col);
		
		
	} 
	@Test
	public void test4() throws IOException {
		String t =  UtilFile.loadFileAsString(LineIndexTest.class.getResource("test_with_empty_lines.txt"))  ;
		LineIndex index = new LineIndex(null).buildIndex(t);
		
		LineInfo l = index.getLine(t.indexOf('B'));
		assertEquals(17,l.line);
		assertEquals(6,l.col);
		
		l = index.getLine(t.indexOf('X'));
		assertEquals(21,l.line);
		assertEquals(2,l.col);
		
		l = index.getLine(t.indexOf('Y'));
		assertEquals(23,l.line);
		assertEquals(1,l.col);

		l = index.getLine(t.lastIndexOf("\n\n"));
		assertEquals(21,l.line);
		assertEquals(10,l.col);
		
		l = index.getLine(t.lastIndexOf("\n\n")+1);
		assertEquals(22,l.line);
		assertEquals(1,l.col);
		
	} 
	@Test
	public void test5() throws IOException {
		String t =  UtilFile.loadFileAsString(LineIndexTest.class.getResource("test_with_empty_lines.txt"))  ;
		LineIndex index = new LineIndex(null).buildIndex(t);
		
		assertEquals(t.indexOf('B'),index.getRawposition(17,6));
		
		assertEquals(t.indexOf('X'),index.getRawposition(21,2));
		
		assertEquals(t.indexOf('Y'),index.getRawposition(23,1));
		
		assertEquals(t.lastIndexOf("\n\n"),index.getRawposition(21,10));
		
		assertEquals(t.lastIndexOf("\n\n")+1,index.getRawposition(22,1));
		
	} 
}
