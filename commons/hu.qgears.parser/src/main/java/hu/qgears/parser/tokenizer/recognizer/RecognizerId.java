package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;

public class RecognizerId extends RecognizerConcat {

	public RecognizerId(ITokenType tokenType) {
		super(tokenType);
		addSubToken(new RecognizerIdStart(new TokenType("dummy")), true);
		addSubToken(new RecognizerIdInside(new TokenType("dummy")), false);
	}

}
