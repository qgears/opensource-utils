package hu.qgears.opengl.kmsgl;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

/**
 * Linux KMS JNI wrapper to access kernel mode fullscreen switch into graphics mode using OpenGL.
 */
public class KMSGL {
	private KMSGLNative nat;
//	private NativeImage[] buffers=new NativeImage[2];
	/**
	 * Load native libraries.
	 */
	public KMSGL()
	{
		KMSGLInstance.getInstance();
		nat=new KMSGLNative();
	}
	/**
	 * switch to fullscreen double buffered, vsynced.
	 */
	public void enterKmsFullscreen()
	{
		nat.init("/dev/dri/card0");
		for(int i=0;i<2;++i)
		{
			int w=nat.getBufferParam(0, i, 0);
			int h=nat.getBufferParam(0, i, 1);
//			int stride=nat.getBufferParam(0, i, 2);
//			ByteBuffer buffer=nat.getBufferPtr(0, i);
//			INativeMemory mem=new DefaultJavaNativeMemory(buffer);
//			buffers[i]=NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.RGBA, mem, stride);
		}
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
