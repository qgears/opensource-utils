package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerNumber extends RecognizerAnyLetter {

	public RecognizerNumber(ITokenType tokenType) {
		super(tokenType, new LetterAcceptorNumber());
	}

}
