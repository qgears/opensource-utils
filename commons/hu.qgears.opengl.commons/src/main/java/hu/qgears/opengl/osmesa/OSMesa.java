package hu.qgears.opengl.osmesa;

import hu.qgears.images.ENativeImageComponentOrder;
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
	public void createContext(ENativeImageComponentOrder co)
	{
		int mode=0;
		switch (co) {
		case ARGB:
			mode=0;
			break;
		case BGRA:
			mode=1;
			break;
		case RGBA:
			mode=2;
			break;
		default:
			throw new RuntimeException("Component order not handled: "+co);
		}
		n.createContext(mode);
	}
	public  void makeCurrent(NativeImage image)
	{
		n.makeCurrentPrivate(image.getBuffer().getJavaAccessor(), image.getSize().getWidth(), image.getSize().getHeight());
	}
	public void disposeContext()
	{
		n.disposeContext();
	}
	public String getGlVersion()
	{
		return n.getGlVersion();
	}
}
