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
	protected native boolean processEvents();
}
