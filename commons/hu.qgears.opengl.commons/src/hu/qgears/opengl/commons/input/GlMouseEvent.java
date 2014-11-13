package hu.qgears.opengl.commons.input;

import java.util.List;

/**
 * Generic mouse event DTO class.
 * Stores an atomic mouse event that can be either a movement of the mouse (either normal move or drag)
 * or a movement+mouse down/up event.
 * @author rizsi
 *
 */
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
	@Override
	public String toString() {
		return "["+x+", "+y+"; "+button+": "+buttonState+"]";
	}
	public void parseFromLog(List<String> pieces)
	{
		nanoseconds=Long.parseLong(pieces.get(0));
		x=Integer.parseInt(pieces.get(2));
		y=Integer.parseInt(pieces.get(3));
		button=EMouseButton.parseSafe(Integer.parseInt(pieces.get(4)));
		buttonState=Boolean.parseBoolean(pieces.get(5));
	}
	public String serializeToLog(long t) {
		StringBuilder ret=new StringBuilder();
		ret.append(""+t);
		ret.append(" 0 ");
		ret.append(""+x);
		ret.append(" ");
		ret.append(""+y);
		ret.append(" ");
		ret.append(""+EMouseButton.ordinalSafe(button));
		ret.append(" ");
		ret.append(""+buttonState);
		return ret.toString();
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
		x=y=0;
		button=null;
		nanoseconds=0;
		buttonState=false;
	}
}
