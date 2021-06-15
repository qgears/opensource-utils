package hu.qgears.parser.tokenizer.impl;

public class LanguageParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public LanguageParseException() {
		super();
	}

	public LanguageParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public LanguageParseException(String message) {
		super(message);
	}

	public LanguageParseException(Throwable cause) {
		super(cause);
	}

}
