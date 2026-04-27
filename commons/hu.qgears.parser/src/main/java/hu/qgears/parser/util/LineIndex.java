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
	 * @param rawPosition The index of the character/cursor-position to map to line info.
	 * @return
	 */
	public LineInfo getLine(int rawPosition) {
		//let's allow rawPosition == contentSize
		//consider the following:
		//contentSize == 0, and we would like to resolve raw positions describing a range: [0,0)
		//0 is a sensible offset, referring to the only valid cursor position
		//(character [0] does not exist but that is fine)
		//the return value should be the one and only line.
		if (rawPosition < 0 || contentSize < rawPosition) {
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

	/**
	 * Quasi-inclusive: In and empty file, the start of line 1 is character 1, although there is no character 1.
	 * Combined with the line end: [1,1) makes sense, and we can recognize it as an empty range.
	 * Naturally the same goes for an empty line at the end of the file.
	 *
	 * @param line1 1-based
	 * @return quasi-inclusive
	 */
	public int getLineStartOffset(int line1) {
		if (line1 < 1) {
			throw new IndexOutOfBoundsException(line1);
		} else if (line1 == 1) {
			return 0;
		} else {
			int iPrevLineEnd = line1 - 2;
			if (0 <= iPrevLineEnd && iPrevLineEnd < lineBreakPositionsSize) {
				return lineBreakPositions[iPrevLineEnd] + 1;
			} else /*iPrevLineEnd out of range*/ {
				throw new IndexOutOfBoundsException(line1);
			}
		}
	}

	/**
	 * The (carriage-return and the) line-feed is considered part of the line.
	 *
	 * @param line1 1-based
	 * @return exclusive
	 */
	public int getLineEndOffset(int line1) {
		int nLines = lineBreakPositionsSize + 1;
		if (line1 < 1) {
			throw new IndexOutOfBoundsException(line1);
		} else if (line1 < nLines) { //not the last line
			int iLineEnd = line1 - 1;
			int offsetLineEndInclusive = lineBreakPositions[iLineEnd];
			return offsetLineEndInclusive + 1;
		} else if (line1 == nLines) { //last line
			return contentSize;
		} else /*nLines < line1*/ {
			throw new IndexOutOfBoundsException(line1);
		}
	}
}
