package hu.qgears.commons.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvImpl implements CsvEditor, CsvContent{

	private int preferredWidth;

	private List<List<String>> data = new ArrayList<>(); 
	private List<String> cline;
	
	private Map<Integer,Integer> maxColLengs;
	private int colCnt;

	private String delimiter;
	
	public CsvImpl() {
		data = new ArrayList<>();
		maxColLengs = new HashMap<>();
		colCnt = 0;
		delimiter = ",";
	}
	
	@Override
	public void addData(String data) {
		if (cline == null) {
			newLine();
		}
		cline.add(data);
		updateMaxLength(data.length());
		colCnt++;
	}

	private void updateMaxLength(int length) {
		int mx = 0;
		if (maxColLengs.containsKey(colCnt)) {
			mx = maxColLengs.get(colCnt);
		}
		if (getLineCount() >1) {
			maxColLengs.put(colCnt,Math.max(mx, length));
		} else {
			//skip first line, typically headers
		}
	}

	@Override
	public void newLine() {
		List<String> line = new ArrayList<>();
		data.add(line);
		cline = line;
		colCnt = 0;
	}

	@Override
	public void setPreferredDataWidth(int width) {
		this.preferredWidth = width;
	}

	@Override
	public String getContent() {
		StringBuilder bld = new StringBuilder();
		int lines = getLineCount();
		int cols = getColumnCount();
		
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j < cols ; j++) {
				printCellFormatted(i,j,bld);
				if (j != cols-1) {
					bld.append(delimiter);
				}
			}
			bld.append("\n");
		}
		return bld.toString();
	}

	
	private void printCellFormatted(int i, int j, StringBuilder bld) {
		String cell = getCell(i,j);
		int width = getPreferredWidth(i,j);
		if (width > 0) {
			bld.append(cell);
			if (j < (getColumnCount()-1)) {
				int pad = width - cell.length(); 
				while (pad > 0) {
					bld.append(' ');
					pad--;
				}
			}
		} else {
			bld.append(cell);
		}
	}

	private int getPreferredWidth(int i, int j) {
		if(preferredWidth > 0) {
			int max = maxColLengs.get(j);
			if (max < preferredWidth) {
				return preferredWidth;
			} else {
				return max;
			}
		} else {
			return -1;
		}
	}

	@Override
	public String getCell(int i, int j) {
		if (data.size() > i) {
			List<String> l = data.get(i);
			if (l.size() > j) {
				return l.get(j);
			}
		}
		return "";
	}

	@Override
	public int getColumnCount() {
		int c = 0;
		for (List<String> l : data) {
			c = Math.max(c, l.size());
		}
		return c;
	}
	
	@Override
	public int getLineCount() {
		return data.size();
	}

	@Override
	public void setDelimiter(String del) {
		this.delimiter = del;
	}

}
