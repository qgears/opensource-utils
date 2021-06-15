package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerWhiteSpace extends RecognizerAnyLetter {
	public RecognizerWhiteSpace(ITokenType tokenType) {
		super(tokenType, new Character[] { ' ', '\n', '\t', '\r' });
	}
}
