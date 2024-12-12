package hu.qgears.images.tiff;

/**
 * Describes the data necessary for loading a tiff image. 
 * 
 * @author agostoni
 *
 */
public class ImageDataConnector{

	/**
	 * Pointer for native object represents this class.
	 */
	private long ptr;
	
	protected ImageDataConnector() {
	}
	
	protected native void init();

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

	protected native void disposePrimitive();
}
