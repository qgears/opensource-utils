package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.util.ParseRuntimeException;

public class RecognizerAnyCaseConst extends RecognizerAbstract implements
		ITokenRecognizer {
	String c;

	@Override
	public int getGeneratedToken(TextSource src) {
		String head = src.firstChars(c.length());
		if (head.toUpperCase().equals(c.toUpperCase())) {
			int l=c.length();
			return l;
		} else {
			return 0;
		}
	}

	public RecognizerAnyCaseConst(ITokenType tokenType, String c)
			throws ParseRuntimeException {
		super(tokenType);
		if (c.length() < 1)
			throw new ParseRuntimeException("invalid token: = length constant");
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
