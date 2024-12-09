package hu.qgears.commons.csv;

public interface CsvEditor {

	void addData(String data);
	void newLine();
	void setDelimiter(String del);
	
	void setPreferredDataWidth(int width);
	
	String getContent();
}
