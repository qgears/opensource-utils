package hu.qgears.opengl.kms;

import java.nio.ByteBuffer;

public class KMSNative {
	protected native int init();
	protected native int swapBuffers(int index);
	protected native ByteBuffer getCurrentBackBufferPtr(int index);
	protected native void dispose();
}
