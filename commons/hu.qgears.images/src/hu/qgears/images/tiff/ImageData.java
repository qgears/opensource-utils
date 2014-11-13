package hu.qgears.images.tiff;

import hu.qgears.commons.IDisposeable;

/**
 * Describes the data necessary for loading a tiff image. 
 * 
 * @author agostoni
 *
 */
public class ImageData implements IDisposeable{

	/**
	 * Pointer for native object represents this class.
	 */
	private long ptr;
	
	private boolean disposed = false;
	
	public ImageData() {
		init();
	}
	
	private native void init();

	/**
	 * Returns the image width in pixels.
	 * 
	 * @return
	 */
	public native int getWidth();

	/**
	 * Returns the image height in pixels.
	 * 
	 * @return
	 */
	public native int getHeight();

	/**
	 * Returns a pointer where the RGB image data begins.
	 * 
	 * @return
	 */
	public static native int getPixelDataOffset();

	private native void disposePrimitive();

	@Override
	public void dispose() {
		if (!isDisposed()){
			disposePrimitive();
			disposed = true;
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

}
