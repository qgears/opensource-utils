package hu.qgears.images.tiff;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.commons.mem.WrappedJavaNativeMemory;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.nativeloader.UtilNativeLoader;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLockInterruptionException;


/**
 * Java connector for native tiff loader components.
 * 
 * @author agostoni
 *
 */
public class NativeTiffLoader {
	/**
	 * Size of the TIFF header. Maximum size of the TIFF files is width*height*3+headerSize
	 * Can be used to allocate buffers big enough to store a TIFF file
	 */
	public static final int maximumHeaderSize = 1024;
	private static NativeTiffLoader tiffLoader;
	
	private NativeTiffLoader(){/*only single instance is available*/}
	
	public static NativeTiffLoader getInstance(){
		if (tiffLoader == null){
			UtilNativeLoader.loadNatives(new NativeTiffLoaderAccessor());
			tiffLoader = new NativeTiffLoader();
		}
		return tiffLoader;
	}
	
	/**
	 * Loads tiff image as {@link NativeImage} from
	 * {@link FileLockInterruptionException}. Allocates memory for the image using {@link DefaultJavaNativeMemoryAllocator}
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public NativeImage loadImageFromTiff(File filePath) throws IOException{
		return loadImageFromTiff(filePath, DefaultJavaNativeMemoryAllocator.getInstance());
	}
	/**
	 * Loads tiff image as {@link NativeImage} from
	 * {@link FileLockInterruptionException}.
	 * 
	 * @param filePath
	 * @param allocator
	 * @return
	 * @throws IOException
	 */
	public NativeImage loadImageFromTiff(File filePath, INativeMemoryAllocator allocator) throws IOException{
		INativeMemory  mem=UtilFile.loadAsByteBuffer(filePath, allocator);
		NativeImage ret;
		try {
			ret = loadImageFromTiff(mem);
		} finally {
			mem.decrementReferenceCounter();
		}
		return ret;
	}
	
	/**
	 * TODO proper ByteBuffer creating and disposal need to be done! Currently
	 * for test purposes only.
	 * 
	 * @param fileData
	 * @return
	 */
	public NativeImage loadImageFromTiff(byte[] fileData) throws NativeTiffLoaderException{
		INativeMemory wrap=DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(fileData.length);
		wrap.getJavaAccessor().put(fileData);
		return loadImageFromTiff(wrap) ;
	}
	
	/**
	 * Loads image data from file content specified as {@link ByteBuffer}. The
	 * retrieved {@link NativeImage} uses a {@link WrappedJavaNativeMemory} over
	 * input bytebuffer for referring image data. 
	 * 
	 * @param fileBuffer
	 * @return
	 */
	public NativeImage loadImageFromTiff(INativeMemory fileBuffer) throws NativeTiffLoaderException{
		ImageData image = new ImageData();
		try
		{
			loadTiffImagePrimitive(fileBuffer.getJavaAccessor(),image);
			int offset=ImageData.getPixelDataOffset();
			INativeMemory buffer =new  WrappedJavaNativeMemory(fileBuffer, offset, fileBuffer.getJavaAccessor().capacity());
			SizeInt size = new SizeInt(image.getWidth(),image.getHeight());
			NativeImage ni =new NativeImage(buffer, size, ENativeImageComponentOrder.RGB, 1);
			return ni;
		}finally
		{
			image.dispose();
		}
	}
	

	public void saveImageAsTiff(NativeImage image,File filePath) throws NativeTiffLoaderException{
		if (filePath != null && filePath.getParentFile().isDirectory()){
			saveImageAsTiffPrimitive(image.getWidth(), image.getHeight(), image.getBuffer().getJavaAccessor(),filePath.getAbsolutePath());
		} else {
			throw new NativeTiffLoaderException("Outputfile doesn't exist : "+filePath);
		}
	}
	
	private native void loadTiffImagePrimitive(ByteBuffer fileData, ImageData image) throws NativeTiffLoaderException;
	
	private native void saveImageAsTiffPrimitive(int width,int height,ByteBuffer data, String filePath) throws NativeTiffLoaderException;

}
