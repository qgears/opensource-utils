package hu.qgears.commons.csv;

public interface CsvContent {

	int getLineCount();
	int getColumnCount();

	String getCell(int line, int col);
}
