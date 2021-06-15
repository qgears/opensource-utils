package hu.qgears.parser.tokenizer;

import hu.qgears.parser.impl.ParseException;

public class TokenizerException extends ParseException {
	private static final long serialVersionUID = 1L;

	public TokenizerException() {
		super();
	}

	public TokenizerException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenizerException(String message) {
		super(message);
	}

	public TokenizerException(Throwable cause) {
		super(cause);
	}
}
