package hu.qgears.opengl.commons.input;

public interface IMouse {
	/**
	 * Poll the next event.
	 * @return The returned object is re-usable by the implementation. Fields are only valid until the next call! Null is returned after consuming all events.
	 */
	GlMouseEvent getNextEvent();
	/**
	 * Poll mouse events from the operating system. Move the events to this object's buffer.
	 */
	void poll();

	boolean isButtonDown(EMouseButton b);

	int getX();

	int getY();
	
	/**
	 * For testing purposes only: allows the caller to append an event to the 
	 * low level input event processing queue.
	 * @param type the type of the event - 4:button other:move
	 * @param x x coordinate of the event
	 * @param y y coordinate of the event
	 * @param button the mouse button
	 * @param state state of the mouse button state!=1 means button is down
	 */
	void addEvent(int type, int x, int y, EMouseButton button, int state);
}
