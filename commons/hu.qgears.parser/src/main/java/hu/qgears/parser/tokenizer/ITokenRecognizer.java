package hu.qgears.parser.tokenizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;

/**
 * Tokenizer module that recognizes a token on a stream.
 */
public interface ITokenRecognizer {
	/**
	 * Get the generated token on the text source.
	 * When found a token then:
	 *  * token is added to the ret array
	 *  * number of characters processed is returned.
	 * 
	 * @return 0 if the token can not be recognized
	 */
	int getGeneratedToken(char[] array, int at);

	/**
	 * Get the token types that are recognized by this recognizer.
	 * @return
	 */
	ITokenType getRecognizedTokenTypes();

	/**
	 * In case the token is used in the grammar with restriction then this method is used to create
	 * a matcher.
	 * @param matchingString
	 * @return
	 */
	Matcher createMatcher(String matchingString);
	
	/**
	 * Collect proposals by this token recognizer. (Used by the text editor.)
	 * @param tokenTypeName
	 * @param prefix
	 * @param collector
	 */
	void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector);

	/**
	 * Check if token can start with the given character.
	 * Used to optimize tokenization by filtering possible types by the next character of the input
	 * @param c
	 * @return true means that the token can start with the character. false means this token type never starts with the character.
	 */
	boolean tokenCanStartWith(char c);
}
