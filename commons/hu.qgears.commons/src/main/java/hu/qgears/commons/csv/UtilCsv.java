package hu.qgears.commons.csv;

import java.io.IOException;

/**
 * @author agostoni
 * TODO cleanup API, support read
 */
public class UtilCsv {

	
	private UtilCsv () {}
	
	public static CsvLoader newCsvLoader() throws IOException {
		return new CsvLoader();
	}
	public static CsvLoader newCsvLoader( String delim,String lineComment) throws IOException {
		return new CsvLoader(delim, lineComment);
	}
	
	public static CsvEditor newCsvEditor() {
		return new CsvImpl();
	}
	
}
