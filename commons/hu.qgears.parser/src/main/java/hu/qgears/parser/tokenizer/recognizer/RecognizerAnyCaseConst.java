package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;
import hu.qgears.parser.tokenizer.TokenizerException;

public class RecognizerAnyCaseConst extends RecognizerAbstract implements
		ITokenRecognizer {
	String c;

	@Override
	public IToken getGeneratedToken(ITextSource src) {
		String head = src.firstChars(c.length());
		if (head.toUpperCase().equals(c.toUpperCase())) {
			return new SimpleToken(getTokenType(), src, c.length());
		} else {
			return null;
		}
	}

	public RecognizerAnyCaseConst(ITokenType tokenType, String c)
			throws TokenizerException {
		super(tokenType);
		if (c.length() < 1)
			throw new TokenizerException("invalid token: = length constant");
		this.c = c;
	}
}
