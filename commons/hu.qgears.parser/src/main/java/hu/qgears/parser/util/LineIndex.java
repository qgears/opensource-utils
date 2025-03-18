package hu.qgears.parser.util;

import java.util.Arrays;

/**
 * Utility to convert raw character position to line and column info and vice versa. 
 * <p>
 * Usage:
 * <pre>
 * LineIndex l = new LineIndex(myFile).buildIndex(fileContent);
 * LineInfo li = l.getLine(2345);
 * </pre>
 * </p>
 * @author agostoni
 *
 */
public class LineIndex {

	private String sourceFile;
	private int[] lineBreakPositions;
	private int lineBreakPositionsSize;
	private int contentSize;
	public class LineInfo {
		public int line;
		public int col;
		public String srcFile;
		public LineInfo(int line, int col) {
			super();
			this.line = line;
			this.col = col;
			this.srcFile = sourceFile;
		}
		
		
	}
	
	/**
	 * @param f The indexed file. Optional argument, to set up
	 *          {@link LineInfo#srcFile} as well (useful for generating error
	 *          messages).
	 */
	public LineIndex(String f) {
		sourceFile = f;
	}
	
	/**
	 * Builds up the line index from specified file content.
	 * @param content
	 */
	public LineIndex buildIndex(String content) {
		contentSize = content.length();
		int estimatedLineCount = Math.max(10, contentSize / 40);
		
		lineBreakPositions = new int[estimatedLineCount];
		int lineBreakPositionsIdx = 0;
		for (int i = 0; i < contentSize; i++) {
			char c = content.charAt(i);
			//counting \n only work on windows as well
			if (c == '\n') {
				if (lineBreakPositionsIdx >= lineBreakPositions.length) {
					lineBreakPositions=Arrays.copyOf(lineBreakPositions, lineBreakPositions.length*2);
				}
				lineBreakPositions[lineBreakPositionsIdx] = i;
				lineBreakPositionsIdx++;
			}
		}
		lineBreakPositionsSize = lineBreakPositionsIdx;
		return this;
	}
	
	/**
	 * Computes the {@link LineInfo} from the raw position.
	 * @param rawPosition The index of the character to map to line info.
	 * @return
	 */
	public LineInfo getLine(int rawPosition) {
		if (rawPosition < 0 || rawPosition >= contentSize) {
			throw new IndexOutOfBoundsException(rawPosition);
		}
		int index = Arrays.binarySearch(lineBreakPositions, 0,lineBreakPositionsSize,rawPosition); 
		int insertionPoint;
		if (index < 0) {
			insertionPoint =(index * -1) - 1;
			
		} else {
			//exact match
			insertionPoint = index;
		}
		if (insertionPoint == 0) {
			return new LineInfo(insertionPoint+1,rawPosition +1);
		} else {
			return new LineInfo(insertionPoint+1,rawPosition - lineBreakPositions[insertionPoint-1]);
		}
	}
	
	/**
	 * Returns the raw position of the character at specified line info.
	 * @param lineInfo
	 * @return
	 */
	public int getRawposition(LineInfo lineInfo) {
		return getRawposition(lineInfo.line,lineInfo.col);
	}
	
	public int getRawposition(int line, int col) {
		int lIdx;
		if (line == 1) {
			return col-1;
		} else {
			lIdx = line -2;
			if (lIdx < lineBreakPositionsSize && lIdx >= 0) {
				return lineBreakPositions[lIdx] + col;
			} else {
				throw new IllegalArgumentException("Invalid line "+line);
			}
		}
	}
	public String getSourceFile() {
		return sourceFile;
	}
	
	public LineInfo getLastLine(){
		return getLine(contentSize-1);
	}
}
