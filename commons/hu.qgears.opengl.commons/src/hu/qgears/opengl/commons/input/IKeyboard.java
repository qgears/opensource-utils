package hu.qgears.opengl.commons.input;

public interface IKeyboard {

	/**
	 * 
	 * @return true in case we have at least one unprocessed keyboard event on the
	 * event stack
	 */
	boolean next();

	/**
	 * The LWGJL event key of the currently processed event.
	 * @return LWJGL event key in case of special keys, may be 1 in case of unicode characters
	 */
	int getEventKey();

	/**
	 * Is this a key down event?
	 * @param eventKey
	 * @return
	 */
	boolean isKeyDown();
	
	boolean isSpecialKey();

	/**
	 * Get the character of the current event.
	 * @return 0 in case it is not a character key
	 */
	char getEventCharacter();
	boolean isCtrl();
	boolean isShift();
	boolean isAlt();
}
