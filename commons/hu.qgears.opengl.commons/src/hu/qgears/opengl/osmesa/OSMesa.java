package hu.qgears.opengl.osmesa;

import hu.qgears.images.NativeImage;

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
	OSMesaNative n=new OSMesaNative();
	public void createContext()
	{
		n.createContext();
	}
	public  void makeCurrent(NativeImage image)
	{
		n.makeCurrentPrivate(image.getBuffer().getJavaAccessor(), image.getSize().getWidth(), image.getSize().getHeight());
	}
	public void disposeContext()
	{
		n.disposeContext();
	}
}
