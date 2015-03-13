package hu.qgears.opengl.commons.example;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.libpng.NativeLibPng;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.osmesa.OSMesa;
import hu.qgears.opengl.osmesa.OSMesaInstance;

import java.io.File;

import lwjgl.standalone.BaseAccessor;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 * Render using osmesa - off-screen software renderer.
 * 
 * Use: (see osmesapreload.c)
// export LD_LIBRARY_PATH='pathtoosmesapreloadfolder'/linGL.so.1
// run the lwjgl application that uses osmesa
 * 
 * @author rizsi
 *
 */
public class OSMesaExample {
	public static void main(String[] args) throws Exception {
		BaseAccessor.initLwjglNatives();
		OSMesaInstance.getInstance();
		OSMesa osMesa=new OSMesa();
		osMesa.createContext();
		NativeImage im=NativeImage.create(new SizeInt(1024, 768), ENativeImageComponentOrder.RGBA, DefaultJavaNativeMemoryAllocator.getInstance());
		osMesa.makeCurrent(im);
		GLContext.useContext(osMesa);
		UtilGl.drawMinimalScene();
		GL11.glFinish();
		osMesa.disposeContext();
		new NativeLibPng().saveImage(im, new File("/tmp/gl.png"));
	}
}
