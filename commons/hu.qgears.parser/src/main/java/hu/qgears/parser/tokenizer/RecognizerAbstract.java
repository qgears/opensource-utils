package hu.qgears.parser.tokenizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.impl.TextSource;



public abstract class RecognizerAbstract implements ITokenRecognizer {
	protected ITokenType tokenType;

	abstract public int getGeneratedToken(TextSource src);

	public RecognizerAbstract(ITokenType tokenType) {
		super();
		this.tokenType = tokenType;
	}

	public ITokenType getRecognizedTokenTypes() {
		return tokenType;
	}

	final public ITokenType getTokenType() {
		return tokenType;
	}
	final public int getTokenTypeId() {
		return tokenType.getId();
	}
	@Override
	public Matcher createMatcher(String matchingString) {
		return new Matcher(true, matchingString);
	}
}
