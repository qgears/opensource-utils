package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerWord extends RecognizerAnyLetter {

	public RecognizerWord(ITokenType tokenType) {
		super(tokenType, new LetterAcceptorWord());
	}

}
