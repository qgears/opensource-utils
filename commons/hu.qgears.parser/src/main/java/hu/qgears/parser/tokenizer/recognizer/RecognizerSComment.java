package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.TokenizerException;

public class RecognizerSComment extends RecognizerConcat {
	@Override
	public IToken getGeneratedToken(ITextSource _src) {
		return super.getGeneratedToken(_src);
	}

	public RecognizerSComment(ITokenType tokenType) throws TokenizerException {
		super(tokenType);
		addSubToken(new RecognizerConst(new TokenType("dummy"), "//"), true);
		addSubToken(new RecognizerAnyLetter(new TokenType("dummy"),
				new ILetterAcceptor() {
					public boolean accept(char ch) {
						return ch != '\n';
					}
				}), false);
	}

}
