package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerIdInside extends RecognizerAnyLetter {

	public RecognizerIdInside(ITokenType tokenType) {
		super(tokenType, new LetterAcceptorId());
	}

}
