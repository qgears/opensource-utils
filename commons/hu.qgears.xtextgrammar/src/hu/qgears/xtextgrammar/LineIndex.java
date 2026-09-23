package hu.qgears.xtextgrammar;

import java.util.Arrays;

public final class LineIndex {
	private final int[] lineStarts;
	public LineIndex(CharSequence content) {
		int lineCount = 1;
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '\n') 
				lineCount++;
		}
		lineStarts = new int[lineCount];
		lineStarts[0] = 0;
		int line = 0;
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '\n') {
				line++;
				lineStarts[line] = i + 1;
			}
		}
	}
	public int lineAt(int offset) {
		int index = Arrays.binarySearch(lineStarts, offset);
		if (index >= 0) {
			return index;
		}
		return -index -2;
	}
	public int columnAt(int offset) {
		int line = lineAt(offset);
		return offset - lineStarts[line];
	}
	public int offsetAt(int line, int column) {
		return lineStarts[line] + column;
	}
	public int lineCount() {
		return lineStarts.length ;
	}
}