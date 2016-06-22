package hu.qgears.images.palette;

import java.io.File;

import hu.qgears.images.NativeImage;
import hu.qgears.images.libpng.NativeLibPng;

/**
 * Example usage of the quantize algorithm.
 * @author rizsi
 *
 */
public class QuantizeExample {
	public static void main(String[] args) throws Exception{
		new QuantizeExample().run();
	}
	void run() throws Exception
	{
		File dir=new File("/tmp/in.png");
		NativeImage im=NativeLibPng.loadImage(dir);
		Palette p=QuantizeOctTree.quantizeOctTree(im, 256, null);
		p.reduceImageColorsToPalette(im);
		new NativeLibPng().saveImage(im, new File("/tmp/quantized.png"));
	}
}
