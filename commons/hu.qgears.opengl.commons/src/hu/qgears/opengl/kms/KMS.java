package hu.qgears.opengl.kms;

import java.nio.ByteBuffer;

/**
 * Linux KMS JNI wrapper to access kernel mode fullscreen switch into graphics mode.
 */
public class KMS {
	private KMSNative nat;
	/**
	 * Load native libraries.
	 */
	public KMS()
	{
		KMSInstance.getInstance();
		nat=new KMSNative();
	}
	/**
	 * switch to fullscreen double buffered, vsynced.
	 */
	public void enterKmsFullscreen()
	{
		nat.init();
	}
	/**
	 * Swap current front/backbuffer.
	 * @param index
	 * @return
	 */
	public int swapBuffers(int index)
	{
		return nat.swapBuffers(index);
	}
	public ByteBuffer getCurrentBackBufferPtr(int index)
	{
		return nat.getCurrentBackBufferPtr(index);
	}
	public void dispose()
	{
		nat.dispose();
	}

}
