package hu.qgears.opengl.commons.input;

/**
 * Generic mouse event DTO class.
 * Stores an atomic mouse event that can be either a movement of the mouse 
 * (either normal move or drag) or a movement+mouse down/up event.
 * @author rizsi
 *
 */
// Suppressing warnings because of DTO class
@SuppressWarnings("squid:ClassVariableVisibilityCheck")
public class GlMouseEvent {
	/** 
	 * Location where the event has happened. X coordinate.
	 */
	public int x;
	/** 
	 * Location where the event has happened. Y coordinate.
	 */
	public int y;
	/**
	 * The button that has changed its state. null means that no button has
	 * changed its state.
	 */
	public EMouseButton button;
	/**
	 * The new state of the button that has changed its state.
	 */
	public boolean buttonState;
	/**
	 * Timestamp when the mouse event has happened.
	 */
	public long nanoseconds;
	
	/**
	 * The id of the element on which the mouse event eventually occurred.
	 */
	public String affectedElementIds;
	
	@Override
	public String toString() {
		return "["+x+", "+y+"; "+button+": "+buttonState+"]";
	}

	public void clone(GlMouseEvent ev) {
		x=ev.x;
		y=ev.y;
		nanoseconds=ev.nanoseconds;
		button=ev.button;
		buttonState=ev.buttonState;
	}
	
	/**
	 * Clear the stored values on this mouse event object for reuse.
	 */
	public void clear()
	{
		x=0;
		y=0;
		button=null;
		nanoseconds=0;
		buttonState=false;
	}
}
