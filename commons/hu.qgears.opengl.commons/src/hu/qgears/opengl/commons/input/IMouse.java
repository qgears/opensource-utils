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
}
