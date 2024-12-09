package hu.qgears.commons.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

public class CsvLoader {
	
	private String delimiter = ",";
	private String lineComment = "#";
	public CsvContent loadCsv(InputStream in) throws IOException {
		return loadCsv(
				new BufferedReader(new InputStreamReader(in,Charset.forName("UTF-8"))));
	}

	public CsvContent loadCsv(File in ) throws IOException {
		return loadCsv(new FileInputStream(in));
	}
	
	public CsvContent loadCsv(String in) throws IOException {
		return loadCsv(new BufferedReader(new StringReader(in)));
	}
	
	public CsvContent loadCsv(BufferedReader bfr) throws IOException {
		CsvImpl i = new CsvImpl();
		i.setDelimiter(delimiter);
		try  {
			while (bfr.ready()) {
				String l = bfr.readLine();
				if (l == null) {
					break;
				}
				if (lineComment == null ||  !l.startsWith(lineComment)) {
					String parts[] = l.split(delimiter);
					i.newLine();
					for (String p : parts) {
						i.addData(p.trim());
					}
				}
			}
		} finally {
			bfr.close();
		}
		return i;
	}
	
	
	public CsvLoader() {
		this(",","#");
	}
	public CsvLoader(String delimiter, String lineComment) {
		super();
		this.delimiter = delimiter;
		this.lineComment = lineComment;
	}

	public void setLineComment(String lineComment) {
		this.lineComment = lineComment;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}
