package hu.qgears.parser.coloring;

public class ParseErrorFeedback {
	public static String key=ParseErrorFeedback.class.getSimpleName();
	public final String message;
	public final int position;
	public final int length;
	public ParseErrorFeedback(String message, int position, int length) {
		this.message=message;
		this.position=position;
		this.length=length;
	}
}
