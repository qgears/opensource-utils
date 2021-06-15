package hu.qgears.parser.tokenizer;

import java.util.Collections;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;



public abstract class RecognizerAbstract implements ITokenRecognizer {
	protected ITokenType tokenType;

	abstract public IToken getGeneratedToken(ITextSource src);

	public RecognizerAbstract(ITokenType tokenType) {
		super();
		this.tokenType = tokenType;
	}

	public List<ITokenType> getRecognizedTokenTypes() {
		return Collections.singletonList(tokenType);
	}

	public ITokenType getTokenType() {
		return tokenType;
	}
	@Override
	public Matcher createMatcher(String matchingString) {
		return new Matcher(true, matchingString);
	}
}
