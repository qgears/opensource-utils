package hu.qgears.sdlwindow;

import java.nio.ByteBuffer;

/**
 * GUI Window with SDL implementation
 */
public class SdlWindowNative {
	protected SdlWindowNative() {
	}
	protected native void init();
	protected native void openWindow(int w, int h, String windowName);
	protected native void drawExample(ByteBuffer pixelBuffer);
	protected native void updateFrame(ByteBuffer pixelBuffer);
	protected native void closeWindow();
	/**
	 * Read an event into the buffer.
	 * @param event
	 * @return false means there is no event in the queue. true means one event was read into the buffer.
	 */
	protected native boolean pollEvent(ByteBuffer event);
}
