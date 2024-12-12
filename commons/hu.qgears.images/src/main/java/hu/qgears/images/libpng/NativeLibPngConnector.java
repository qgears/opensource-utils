package hu.qgears.images.libpng;

import java.nio.ByteBuffer;

public class NativeLibPngConnector {
	private long ptr;
	/**
	 * Number of channels in the PNG image.
	 * Valid values:
	 *  * 1 - monochrome image or alpha mask
	 *  * 3 - RGB image
	 *  * 4 - ARGB image
	 * @return
	 */
	protected native int getNumberOfChannels();
	protected native void loadImage(ByteBuffer javaAccessor, int rowBytes);
	/**
	 * Free all resources that were allocated in the current load image process.
	 */
	protected native void closeLoad();
	/**
	 * Parse the header of the image file.
	 * State change: in all cases (even if the method throws exception)
	 * the closeLoad method must be called finally to release all allocated resources 
	 * @param imageData contains all bytes of the PNG image
	 */
	protected native void beginLoad(ByteBuffer imageData) throws NativeLibPngException;
	/**
	 * Get the size of the image that is to be loaded.
	 * Returns valid data after beginLoad returned without error.
	 * @return
	 */
	protected native int getHeight();
	/**
	 * Get the size of the image that is to be loaded.
	 * Returns valid data after beginLoad returned without error.
	 * @return
	 */
	protected native int getWidth();
	protected native int getRowBytes();

	/*
	 * NOSONAR : too many parameters is OK. passing primitive parameters is
	 * easier in JNI
	 */
	protected native void beginSave(int width, int height, int rowBytes,//NOSONAR
			int nChannel,
			boolean swapAplha,
			boolean swapBGR,
			boolean premultipliedAlpha,
			ByteBuffer pixelData);
	protected native int getFileSize();
	protected native void saveImage(ByteBuffer file);
	/**
	 * Release all resources that were allocated in the current save process.
	 */
	protected native void closeSave();
}
