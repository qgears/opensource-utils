package hu.qgears.images.test;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.images.NativeImage;
import hu.qgears.images.libpng.NativeLibPng;

import java.io.File;
import java.nio.ByteBuffer;

public class TestLibPng {

	public static void main(String[] args) throws Exception {
		new TestLibPng().run();
	}

	private void run() throws Exception {
		byte[] data=UtilFile.loadFile(getClass().getResource("1292.png"));
		ByteBuffer imageData=ByteBuffer.allocateDirect(data.length);
		imageData.put(data);
		NativeImage im=new NativeLibPng().loadImage(imageData, DefaultJavaNativeMemoryAllocator.getInstance(), 4);
		INativeMemory pngFile=new NativeLibPng().saveImage(im, DefaultJavaNativeMemoryAllocator.getInstance());
		UtilFile.saveAsFile(new File("/tmp/a.png"), pngFile.getJavaAccessor());
	}
}
