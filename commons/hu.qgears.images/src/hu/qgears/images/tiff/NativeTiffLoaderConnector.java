package hu.qgears.images.tiff;

import java.nio.ByteBuffer;


/**
 * Java connector for native tiff loader components.
 * 
 * @author agostoni
 *
 */
public class NativeTiffLoaderConnector {
	/**
	 * Size of the TIFF header. Maximum size of the TIFF files is width*height*3+headerSize
	 * Can be used to allocate buffers big enough to store a TIFF file
	 */
	public static final int maximumHeaderSize = 1024;
	
	protected NativeTiffLoaderConnector(){/*only single instance is available*/}
	
	protected native void loadTiffImagePrimitive(ByteBuffer fileData, ImageDataConnector image) throws NativeTiffLoaderException;
	
	protected native void saveImageAsTiffPrimitive(int width,int height,ByteBuffer data, String filePath) throws NativeTiffLoaderException;

}
