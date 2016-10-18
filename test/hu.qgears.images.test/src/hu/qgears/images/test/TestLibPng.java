package hu.qgears.images.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.UtilNativeImageIo;
import hu.qgears.images.libpng.NativeLibPng;
import hu.qgears.images.swing.UtilSwing;

/**
 * Test LibPNG based image load and save methods by comparing the result to the Swing implementation.
 */
public class TestLibPng {
	/**
	 * Test loading an image from a PNG file saving it again an loading again
	 * and compare all phases to the Swing implementation of PNG loading.
	 * @throws IOException
	 */
	@Test
	public void testSaveAndLoad() throws IOException
	{
		byte[] data=UtilFile.loadFile(getClass().getResource("1292.png"));
		testSaveAndLoad(data);
		data=UtilFile.loadFile(getClass().getResource("1296.png"));
		testSaveAndLoad(data);
	}
	private void testSaveAndLoad(byte[] data) throws IOException {
		ByteBuffer imageData=ByteBuffer.allocateDirect(data.length);
		imageData.put(data);
		NativeImage im=new NativeLibPng().loadImage(imageData, DefaultJavaNativeMemoryAllocator.getInstance(), 4);
		INativeMemory pngFile=new NativeLibPng().saveImage(im, DefaultJavaNativeMemoryAllocator.getInstance());
		ByteBuffer savedData=pngFile.getJavaAccessor();
		byte[] savedDataArr=new byte[savedData.remaining()];
		savedData.get(savedDataArr);
		NativeImage reference=loadUsingSwing(data);
		NativeImage reloaded=loadUsingSwing(savedDataArr);
		String diff=UtilNativeImageIo.isEqual(reference, im);
		Assert.assertEquals("Compare to image loaded using Swing loader", null, diff);
		diff=UtilNativeImageIo.isEqual(reference, reloaded);
		Assert.assertEquals("Compare saved/reloaded image to reference", null, diff);
	}
	/**
	 * Load the file using the Swing PNG loader (reference for testing the LibPNG loader).
	 * @param data
	 * @return the image loaded and converted to {@link ENativeImageComponentOrder}.RGB format.
	 * @throws IOException
	 */
	private NativeImage loadUsingSwing(byte[] data) throws IOException {
		BufferedImage bim=ImageIO.read(new ByteArrayInputStream(data));
		NativeImage reference0=UtilSwing.bufferedImageToNativeImage(bim, DefaultJavaNativeMemoryAllocator.getInstance());
		NativeImage reference=NativeImage.create(reference0.getSize(), ENativeImageComponentOrder.RGB, DefaultJavaNativeMemoryAllocator.getInstance());
		reference.copyFromSource(reference0, 0, 0);
		return reference;
	}
}
