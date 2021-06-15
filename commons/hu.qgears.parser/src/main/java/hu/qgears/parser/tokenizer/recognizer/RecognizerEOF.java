package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;

public class RecognizerEOF extends RecognizerAbstract implements
		ITokenRecognizer {
	@Override
	public IToken getGeneratedToken(ITextSource src) {
		return null;
	}

	public RecognizerEOF(ITokenType tokenType) {
		super(tokenType);
	}
}
