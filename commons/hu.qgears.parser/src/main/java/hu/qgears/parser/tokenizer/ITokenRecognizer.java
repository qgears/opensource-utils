package hu.qgears.parser.tokenizer;

import java.util.List;
import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;

/**
 * Tokenizer module that recognizes a token on a stream.
 */
public interface ITokenRecognizer {
	/**
	 * Get the generated token on the text source.
	 * 
	 * @param src
	 * @return null if the token can not be recognized
	 */
	IToken getGeneratedToken(ITextSource src);

	/**
	 * Get the token types that are recognized by this recognizer.
	 * @return
	 */
	List<ITokenType> getRecognizedTokenTypes();

	/**
	 * In case the token is used in the grammar with restriction then this method is used to create
	 * a matcher.
	 * @param matchingString
	 * @return
	 */
	Matcher createMatcher(String matchingString);
	
	/**
	 * Collect proposals by this token recognizer.
	 * @param tokenTypeName
	 * @param prefix
	 * @param collector
	 */
	void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector);
}
