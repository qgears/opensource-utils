package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerConst extends RecognizerAbstract implements
		ITokenRecognizer {
	String c;
	private boolean wholeWord=false;
	@Override
	public int getGeneratedToken(TextSource src) {
		String head = src.firstChars(c.length());
		if (head.equals(c)) {
			Character next=src.getCharAt(c.length());
			if(wholeWord && next!=null && Character.isJavaIdentifierPart(next))
			{
				return 0;
			}
			int l=c.length();
			return l;
		} else {
			return 0;
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

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		if(c.startsWith(prefix))
		{
			collector.accept(c);
		}
	}
}
