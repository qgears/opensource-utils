package hu.qgears.opengl.libinput;

import java.nio.ByteBuffer;

public class LibinputNative {
	protected native int init();
	protected native int poll();
	protected native void dispose();
	protected native ByteBuffer getInputBuffer();
	protected native int getInputBufferStrip();
}
