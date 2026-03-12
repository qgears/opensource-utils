package hu.qgears.opengl.commons.example;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.libpng.NativeLibPng;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.osmesa.Log4Init;
import hu.qgears.opengl.osmesa.OSMesa;
import hu.qgears.opengl.osmesa.OSMesaInstance;

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
	private static final int WIDTH = 1024;
	private static final int HEIGHT = 768;
	
	public static void main(String[] args) throws Exception {
		Log4Init.init();
		try {
			new OSMesaExample().run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void run() throws Exception {
		OSMesaInstance.getInstance();
		logLibs();
		OSMesaInstance.getInstance().bindLwjglNatives();
		logLibs();
		OSMesa osMesa=new OSMesa();
		osMesa.createContext(ENativeImageComponentOrder.ARGB);
		NativeImage im=NativeImage.create(new SizeInt(WIDTH, HEIGHT), ENativeImageComponentOrder.RGBA, DefaultJavaNativeMemoryAllocator.getInstance());
		osMesa.makeCurrent(im);
		osMesa.checkOsMesaLoadable();
		System.out.println("Pid: "+getPid());
		GLContext.useContext(osMesa);
		String[] GLExtensions = GL11.glGetString(GL11.GL_EXTENSIONS).split(" ");
		for (int i=0; i < GLExtensions.length; i++) {
			System.out.println(GLExtensions[i]);
		}
		UtilGl.drawMinimalScene();
		GL11.glFinish();
		osMesa.disposeContext();
		new NativeLibPng().saveImage(im, new File("/tmp/gl.png"));
//		System.in.read();
	}
	static private int getPid() throws IOException {
		int pid=Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		return pid;
	}
	static private void logLibs() throws IOException {
		int pid=getPid();
		Process p=new ProcessBuilder("/bin/sh", "-c", "cat /proc/"+pid+"/maps | grep libGL").redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
		UtilProcess.getProcessReturnValueFuture(p);
	}
}
