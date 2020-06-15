package hu.qgears.opengl.kmsgl;

import hu.qgears.images.SizeInt;
import lwjgl.standalone.BaseAccessor;

/**
 * Linux KMS JNI wrapper to access kernel mode fullscreen switch into graphics mode using OpenGL.
 */
public class KMSGL {
	private KMSGLNative nat;
	private SizeInt size;
//	private NativeImage[] buffers=new NativeImage[2];
	/**
	 * Load native libraries.
	 */
	public KMSGL()
	{
		BaseAccessor.noX11=true;
		KMSGLInstance.getInstance();
		nat=new KMSGLNative();
	}
	/**
	 * switch to fullscreen double buffered, vsynced.
	 */
	public void enterKmsFullscreen()
	{
		nat.init("/dev/dri/card0");
		int w=nat.getBufferParam(0, 0, 0);
		int h=nat.getBufferParam(0, 0, 1);
		size=new SizeInt(w, h);
	}
	/**
	 * Swap current front/backbuffer.
	 * @return
	 */
	public int swapBuffers()
	{
		return nat.swapBuffers(0);
	}
	public void dispose()
	{
		nat.dispose();
	}
//	public NativeImage getCurrentBackBuffer() {
//		int i=nat.getCurrentFrontBufferIndex(0);
//		return buffers[i^1];
//	}
	public SizeInt getSize() {
		// TODO Auto-generated method stub
		return new SizeInt(640,480);
	}

}
