package hu.qgears.parser.coloring;

public class Range {
	/** Including */
	public final int from;
	/** Excluding */
	public final int to;
	public final String styleId;
	public Range(int from, int to, String styleId) {
		super();
		this.from = from;
		this.to = to;
		this.styleId = styleId;
	}
}