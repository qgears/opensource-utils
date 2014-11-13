package hu.qgears.opengl.osmesa;

import hu.qgears.images.NativeImage;

import java.nio.ByteBuffer;

/**
 * Currently single instance is supported only!
 * 
 * TODO implement multi instance
 * TODO implement depth, stencil etc buffer support
 * 
 * @author rizsi
 *
 */
public class OSMesa {
	public native void createContext();
	public  void makeCurrent(NativeImage image)
	{
		makeCurrentPrivate(image.getBuffer().getJavaAccessor(), image.getSize().getWidth(), image.getSize().getHeight());
	}
	private native void makeCurrentPrivate(ByteBuffer image, int width, int height);
	public native void disposeContext();
}
