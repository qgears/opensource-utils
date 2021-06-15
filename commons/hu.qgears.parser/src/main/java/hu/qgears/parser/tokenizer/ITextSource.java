package hu.qgears.parser.tokenizer;

/**
 * Abstraction of the text to be parsed. It contains the text and the position
 * of parsing.
 * 
 * @author rizsi
 * 
 */
public interface ITextSource {
	/**
	 * The full sequence of the text.
	 * 
	 * @return
	 */
	CharSequence getFullSequence();

	/**
	 * The subsequence of the text source from the current position.
	 * 
	 * @return
	 */
	CharSequence getCurrentSequence();

	/**
	 * current position of parsing.
	 * 
	 * @return
	 */
	int getPosition();

	/**
	 * Set the current position of parsing
	 * 
	 * @param pos
	 * @return this
	 */
	ITextSource setPosition(int pos);

	/**
	 * Move the position with pass characters.
	 * 
	 * @param pass
	 * @return
	 */
	ITextSource pass(int pass);

	/**
	 * There is no character left.
	 * 
	 * @return
	 */
	boolean isEmpty();

	/**
	 * The first length characters from current position.
	 * 
	 * @param length
	 * @return the first length chars from current position or shorter if less characters are available
	 */
	String firstChars(int length);
	
	/**
	 * Returns true if the sequence starts with the given string
	 * from the given position.
	 * @param relPos
	 * @param s
	 * @return
	 */
	boolean startsWith(int relPos, String s);

	/**
	 * The first length characters from position from.
	 * 
	 * @param from
	 * @param length
	 * @return
	 */
	String firstChars(int from, int length);
	/**
	 * The last length characters from position from.
	 * 
	 * @param from
	 * @param length
	 * @return
	 */
	String lastChars(int from, int length);

	/**
	 * Get the at the given relative position.
	 * 
	 * @param i
	 * @return the next character or null at EOF
	 */
	Character getCharAt(int i);

	/**
	 * Clone this text source. The clone contains the same characters at the
	 * same position. Changeing position on clone does not affect the original
	 * source.
	 * 
	 * @return
	 */
	ITextSource getClone();
	
	/**
	 * Get the length of this text.
	 * @return
	 */
	int getLength();
}
