package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerIdStart extends RecognizerAnyLetter {

	public RecognizerIdStart(ITokenType tokenType) {
		super(tokenType, new LetterAcceptorIdFirst());
	}

}
