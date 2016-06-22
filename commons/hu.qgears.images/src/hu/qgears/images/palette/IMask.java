package hu.qgears.images.palette;

import hu.qgears.images.NativeImage;

/**
 * Pixel mask on an image that can be used to select pixels to omit from an operation.
 * @author rizsi
 */
public interface IMask {
	/**
	 * Is this pixel skipped by this mask?
	 * @param x pixel coordinate
	 * @param y pixel coordinate
	 * @param v color value in {@link NativeImage} format
	 * @return true means that the pixel must be skipped from the operation.
	 */
	boolean skip(int x, int y, int v);
}
