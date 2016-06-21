package hu.qgears.images.palette;

import hu.qgears.images.NativeImage;

/**
 * A palette that holds a set of color that can be used when resampling an image.
 * (can be result of quantization of colors).
 * @author rizsi
 *
 */
public class Palette {
	private int [] colors;
	/**
	 * Create a palette from the colors.
	 * @param colors represented in the {@link NativeImage} color format.
	 */
	public Palette(int[] colors) {
		super();
		this.colors = colors;
	}
	/**
	 * Get the color with the closest index for the given input color.
	 * Distance is counted based on square of difference.
	 * @param color index of the color to find best fit for.
	 * @return index of the best fitting color
	 */
	public int getClosestIndex(int color)
	{
		int v0=(color>>24)&0xFF;
		int v1=(color>>16)&0xFF;
		int v2=(color>>8)&0xFF;
		int bestError=Integer.MAX_VALUE;
		int ret=0;
		for(int i=0;i<colors.length;++i)
		{
			int x=colors[i];
			int x0=(x>>24)&0xFF;
			int x1=(x>>16)&0xFF;
			int x2=(x>>8)&0xFF;
			int error=square(x0-v0)+square(x1-v1)+square(x2-v2);
			if(error<bestError)
			{
				bestError=error;
				ret=i;
			}
		}
		return ret;
	}
	static private int square(int i) {
		return i*i;
	}
	/**
	 * Reduce all pixels of an image to the given palette.
	 * @param im the input image. Pixels are changed within this object so the source image is changed.
	 */
	public void reduceImageColorsToPalette(NativeImage im)
	{
		for(int j=0;j<im.getHeight();++j)
		{
			for(int i=0;i<im.getWidth();++i)
			{
				int v=im.getPixel(i, j);
				int colorIndex=getClosestIndex(v);
				int vNew=colors[colorIndex];
				im.setPixel(i, j, vNew);
			}
		}
	}
}
