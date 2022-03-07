package hu.qgears.parser.tokenizer;

import hu.qgears.parser.impl.ParseException;

public class TokenizerException extends ParseException {
	private static final long serialVersionUID = 1L;
	private int position;

	public TokenizerException(String message, int position) {
		super(message);
		this.position=position;
	}
	public int getPosition() {
		return position;
	}
}
