package hu.qgears.opengl.kmsgl;

import java.nio.ByteBuffer;

public class KMSGLNative {
	protected native int init(String card);
	protected native int swapBuffers(int devIndex);
	/**
	 * 
	 * @param bufferIndex 0 or 1 (double buffering)
	 * @return
	 */
	protected native ByteBuffer getBufferPtr(int devIndex, int bufferIndex);
	/**
	 * 
	 * @param devIndex unused TODO remove
	 * @param bufferIndex unused TODO remove
	 * @param paramIndex
	 * @return
	 */
	protected native int getBufferParam(int devIndex, int bufferIndex, int paramIndex);
	protected native int getCurrentFrontBufferIndex(int devIndex);
	protected native void dispose();
}
