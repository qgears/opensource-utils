package hu.qgears.parser.impl;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;
	private int position=-1;
	public ParseException() {
		super();
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
	public ParseException setPosition(int pos) {
		this.position=pos;
		return this;
	}
	public int getPosition() {
		return position;
	}
}
