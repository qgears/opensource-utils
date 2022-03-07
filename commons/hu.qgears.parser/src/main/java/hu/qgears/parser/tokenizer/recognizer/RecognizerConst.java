package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerConst extends RecognizerAbstract implements
		ITokenRecognizer {
	String c;
	private boolean wholeWord=false;
	@Override
	public IToken getGeneratedToken(ITextSource src) {
		String head = src.firstChars(c.length());
		if (head.equals(c)) {
			Character next=src.getCharAt(c.length());
			if(wholeWord && next!=null && Character.isJavaIdentifierPart(next))
			{
				return null;
			}
			return new SimpleToken(getTokenType(), src, c.length());
		} else {
			return null;
		}
	}

	public RecognizerConst(ITokenType tokenType, String c) {
		this(tokenType, c, false);
	}
	public RecognizerConst(ITokenType tokenType, String c, boolean wholeWord) {
		super(tokenType);
		this.wholeWord=wholeWord;
		if (c.length() < 1)
			throw new IllegalArgumentException("invalid token: = length constant");
		this.c = c;
	}
}
