package hu.qgears.commons.test.csv;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.commons.csv.CsvContent;
import hu.qgears.commons.csv.CsvEditor;
import hu.qgears.commons.csv.UtilCsv;

public class UtilCsvTest {
	@Test
	public void testShortHeaders() {
		CsvEditor csv =UtilCsv.newCsvEditor();
		csv.setPreferredDataWidth(10);
		csv.addData("A");
		csv.addData("B");
		for (int i = 0; i < 150; i+=9) {
			csv.newLine();
			csv.addData("test"+i);
			csv.addData("data"+i);
		}
		String ser = csv.getContent();
		
		Assert.assertEquals("A         ,B\n" + 
				"test0     ,data0\n" + 
				"test9     ,data9\n" + 
				"test18    ,data18\n" + 
				"test27    ,data27\n" + 
				"test36    ,data36\n" + 
				"test45    ,data45\n" + 
				"test54    ,data54\n" + 
				"test63    ,data63\n" + 
				"test72    ,data72\n" + 
				"test81    ,data81\n" + 
				"test90    ,data90\n" + 
				"test99    ,data99\n" + 
				"test108   ,data108\n" + 
				"test117   ,data117\n" + 
				"test126   ,data126\n" + 
				"test135   ,data135\n" + 
				"test144   ,data144\n" 
				, ser);
	}
	
	@Test
	public void testNoPrefferred() {
		CsvEditor csv =UtilCsv.newCsvEditor();
		csv.addData("Normal A");
		csv.addData("Normal B");
		for (int i = 0; i < 150; i+=9) {
			csv.newLine();
			csv.addData("test"+i);
			csv.addData("data"+i);
		}
		String content = csv.getContent();
		Assert.assertEquals("Normal A,Normal B\n" + 
				"test0,data0\n" + 
				"test9,data9\n" + 
				"test18,data18\n" + 
				"test27,data27\n" + 
				"test36,data36\n" + 
				"test45,data45\n" + 
				"test54,data54\n" + 
				"test63,data63\n" + 
				"test72,data72\n" + 
				"test81,data81\n" + 
				"test90,data90\n" + 
				"test99,data99\n" + 
				"test108,data108\n" + 
				"test117,data117\n" + 
				"test126,data126\n" + 
				"test135,data135\n" + 
				"test144,data144\n" 
				, content);
		
	}
	
	@Test
	public void testLongHeaders() {
		CsvEditor csv =UtilCsv.newCsvEditor();
		csv.setPreferredDataWidth(10);
		csv.addData("VeryVeryLongA");
		csv.addData("VeryVeryVeryVeryVeryLongB");
		for (int i = 0; i < 150; i+=9) {
			csv.newLine();
			csv.addData("test"+i);
			csv.addData("data"+i);
		}
		String content = csv.getContent();
		Assert.assertEquals("VeryVeryLongA,VeryVeryVeryVeryVeryLongB\n" + 
				"test0     ,data0\n" + 
				"test9     ,data9\n" + 
				"test18    ,data18\n" + 
				"test27    ,data27\n" + 
				"test36    ,data36\n" + 
				"test45    ,data45\n" + 
				"test54    ,data54\n" + 
				"test63    ,data63\n" + 
				"test72    ,data72\n" + 
				"test81    ,data81\n" + 
				"test90    ,data90\n" + 
				"test99    ,data99\n" + 
				"test108   ,data108\n" + 
				"test117   ,data117\n" + 
				"test126   ,data126\n" + 
				"test135   ,data135\n" + 
				"test144   ,data144\n", content);
	}
	
	@Test
	public void testPreferredLessThanMax() {
		CsvEditor csv =UtilCsv.newCsvEditor();
		csv.setPreferredDataWidth(3);
		csv.addData("A");
		csv.addData("B");
		for (int i = 0; i < 10; i++) {
			csv.newLine();
			csv.addData("longer"+(i+5));
			csv.addData(""+i);
		}
		String ser = csv.getContent();
		
		Assert.assertEquals("A       ,B\n" + 
				"longer5 ,0\n" + 
				"longer6 ,1\n" + 
				"longer7 ,2\n" + 
				"longer8 ,3\n" + 
				"longer9 ,4\n" + 
				"longer10,5\n" + 
				"longer11,6\n" + 
				"longer12,7\n" + 
				"longer13,8\n" + 
				"longer14,9\n" 
				, ser);
	}
	
	@Test
	public void testReadCsv() throws IOException {
		CsvContent content = loadCsv("A;B;\n1;2;\n3;4;\n");
		Assert.assertEquals(3,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	@Test
	public void testReadCsvWithComments() throws IOException {
		CsvContent content = loadCsv("#teszt csv file\nA;B;\n1;2;\n#itt 3 majd négy jön\n3;4;\n",";","#");
		Assert.assertEquals(3,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	private CsvContent loadCsv(String content) throws IOException {
		return UtilCsv.newCsvLoader(";",null).loadCsv(content);
	}
	private CsvContent loadCsv(String content,String delim, String lineComment) throws IOException {
		return UtilCsv.newCsvLoader(delim,lineComment).loadCsv(content);
	}

	@Test
	public void testReadCsvPadded() throws IOException {
		CsvContent content = loadCsv("A   ;B   ;\n1   ; 2  ;\n  3  ; 4  ;\n");
		Assert.assertEquals(3,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	@Test
	public void testReadCsvTabbed() throws IOException {
		CsvContent content = loadCsv("A\t ;B\t  ;\n1  \t ; 2 \t ;\n  3  ; 4  ;\n");
		Assert.assertEquals(3,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	@Test
	public void testReadCsvNoEndTerm() throws IOException {
		CsvContent content = loadCsv("A;B\n1;2\n3;4\n5;6\n");
		Assert.assertEquals(4,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	
	@Test
	public void testReadCsvNoNewLineOnLastRow() throws IOException {
		CsvContent content = loadCsv("A;B\n1;2\n3;4\n5;6");
		Assert.assertEquals(4,content.getLineCount());
		Assert.assertEquals(2,content.getColumnCount());
		
		int c = 1;
		for (int i = 1; i < content.getLineCount(); i++) {
			for (int j = 0; j < content.getColumnCount(); j++) {
				Assert.assertEquals(""+c, content.getCell(i, j));
				c++;
			}
		}
	}
	
}
