package hu.qgears.opengl.commons.input;

/**
 * Mouse button events that can be received from the operating system.
 * @author rizsi
 *
 */
public enum EMouseButton {
	LEFT, MIDDLE, RIGHT,
	WHEEL_UP,
	WHEEL_DOWN, ;

	/**
	 * Convert the ordinal to an enumeration object. Invalid input is converted to null.
	 * @param parseInt
	 * @return enumeration object with index 'parseInt' or null
	 */
	public static EMouseButton parseSafe(int parseInt) {
		if(parseInt>=0&&parseInt<EMouseButton.values().length)
		{
			return EMouseButton.values()[parseInt];
		}
		return null;
	}
	/**
	 * Get the ordinal of the button or return -1 in case of null
	 * @param button
	 * @return
	 */
	public static int ordinalSafe(EMouseButton button) {
		if(button==null)
		{
			return -1;
		}
		return button.ordinal();
	}
}
