package hu.qgears.parser.tokenizer;

import hu.qgears.parser.language.ITokenType;

/**
 * A token that is recognized for a type.
 * A token is a sub-string of the text at a declared position and length.
 * @author rizsi
 *
 */
public interface IToken {
	ITokenType getTokenType();

	/**
	 * The text that generates this token
	 * 
	 * @return
	 */
	CharSequence getText();

	/**
	 * Length of this token.
	 * @return
	 */
	int getLength();

	/**
	 * Position of this token in the text source.
	 * @return
	 */
	int getPos();

	/**
	 * The text source that contains this token.
	 * @return
	 */
	ITextSource getSource();
}
