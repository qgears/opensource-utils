package hu.qgears.parser.language;

/**
 * Language Non-terminal symbol type.
 * 
 * @author rizsi
 * 
 */
public enum EType {
	/**
	 * Concatenation.
	 */
	and,
	/**
	 * Selection.
	 */
	or,
	/**
	 * Zero or more instances of child.
	 */
	zeroormore,
	/**
	 * One or more instances of child.
	 */
	oneormore,
	/**
	 * The epsilon symbol: nothing.
	 */
	epsilon,
	/**
	 * References an other non-terminal type.
	 */
	reference,
	/**
	 * References a simple token type.
	 */
	token
}
