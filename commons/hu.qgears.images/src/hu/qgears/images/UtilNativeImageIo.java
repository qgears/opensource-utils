package hu.qgears.images;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.commons.mem.WrappedJavaNativeMemory;
import hu.qgears.images.libpng.NativeLibPng;
import hu.qgears.images.tiff.NativeTiffLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class UtilNativeImageIo {
	private static int headerSize=24;
	static public int imageToBytesSize(NativeImage im)
	{
		SizeInt size=im.getSize();
		ENativeImageComponentOrder co=im.getComponentOrder();
		return headerSize+co.getNCHannels()*size.getNumberOfPixels();
	}
	/**
	 * Convert an image to a native image format.
	 * 
	 * This image format contains the uncompressed data of the NativeImage
	 * 
	 * @param im
	 * @return
	 */
	static public byte[] imageToBytes(NativeImage im)
	{
		byte[] ret=new byte[imageToBytesSize(im)];
		ByteBuffer bb=ByteBuffer.wrap(ret);
		bb.order(ByteOrder.nativeOrder());
		imageToNativeBytes(im, bb);
		return ret;
	}
	public static boolean isEqual(MappedByteBuffer mbb, NativeImage contents) {
		boolean head=mbb.get()=='Q'&&mbb.get()=='I'&&mbb.get()=='M'&&mbb.get()=='G';
		if(head)
		{
			IntBuffer ib=mbb.asIntBuffer();
			int w=ib.get();
			int h=ib.get();
			int co=ib.get();
			int af=ib.get();
			int mask=ib.get();
			SizeInt size=contents.getSize();
			boolean meta= size.getWidth()==w && size.getHeight()==h && co==contents.getComponentOrder().ordinal()
					&& af==contents.getAlphaStorageFormat().ordinal()
					&& mask==contents.getTransparentOrOpaqueMask();
			if(meta)
			{
				mbb.position(headerSize);
				int step=contents.getStep();
				int lengthOfLine=size.getWidth()*contents.getComponentOrder()
						.getNCHannels();
				ByteBuffer srcBB=contents.getBuffer().getJavaAccessor().duplicate();
				for(int i=0;i<contents.getHeight();++i)
				{
					srcBB.limit(i*step+lengthOfLine);
					srcBB.position(i*step);
					mbb.limit(lengthOfLine*i+headerSize+lengthOfLine);
					mbb.position(lengthOfLine*i+headerSize);
					if(!srcBB.equals(mbb))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * Store the image into a byte buffer.
	 * Target is written from its current position and imageToBytesSize(im) bytes are written.
	 * @param im This image is stored in native uncompressed format.
	 * @param target The remaining size of the target byte buffer must be at least imageToBytesSize(im)
	 */
	static public void imageToNativeBytes(NativeImage im, ByteBuffer target)
	{
		ByteBuffer srcBB=im.getBuffer().getJavaAccessor().asReadOnlyBuffer();
		SizeInt size=im.getSize();
		ENativeImageComponentOrder co=im.getComponentOrder();
		ENativeImageAlphaStorageFormat af=im.getAlphaStorageFormat();
		int initialPosition=target.position();
		target.put((byte)'Q');
		target.put((byte)'I');
		target.put((byte)'M');
		target.put((byte)'G');
		IntBuffer ib=target.asIntBuffer();
		ib.put(size.getWidth());
		ib.put(size.getHeight());
		ib.put(co.ordinal());
		ib.put(af.ordinal());
		ib.put(im.getTransparentOrOpaqueMask());
		target.position(initialPosition+headerSize);
		int step=im.getStep();
		int lengthOfLine=size.getWidth()*co.getNCHannels();
		for(int i=0;i<im.getHeight();++i)
		{
			srcBB.limit(i*step+lengthOfLine);
			srcBB.position(i*step);
			target.put(srcBB);
		}
	}
	
	/**
	 * Saves the passed image as a PNG file. Works for {@link NativeImage}s that 
	 * are handled by {@link NativeLibPng} 
	 * 
	 * @param im
	 * @param f
	 * @throws IOException
	 */
	public static void saveImageToFile(NativeImage im, File f) throws IOException{
		new NativeLibPng().saveImage(im, f);
	}
	/**
	 * Saves the passed image as a PNG file. Works only for {@link NativeImage}s that have 
	 * {@link ENativeImageComponentOrder#RGBA}, {@link ENativeImageComponentOrder#ARGB}.
	 * 
	 * TODO implement for other storage types
	 * @param im
	 * @param f
	 * @throws IOException
	 */
	public static INativeMemory saveImageToBB(NativeImage im) throws IOException{
		INativeMemory ret=new NativeLibPng().saveImage(im, DefaultJavaNativeMemoryAllocator.getInstance());
		return ret;
	}

	/**
	 * Load an image from a native image format. Image of this format can be exported using the 
	 * imageToBytes() method.
	 * This image format contains the uncompressed data of the NativeImage.
	 * 
	 * @param f file to load
	 * @return the native image loaded into memory
	 */
	public static NativeImage loadImageFromFile(File f) throws IOException
	{
		INativeMemory nm=UtilFile.loadAsByteBuffer(f, new DefaultJavaNativeMemoryAllocator());
		nm.getJavaAccessor().order(ByteOrder.nativeOrder());
		return wrapImageFromMemory(nm);
	}
	/**
	 * Parse image header from memory, wrap the memory
	 * and return an image that's content is this part of the memory.
	 * @param nm memory to parse image from.
	 * @return image object with data backed by the memory buffer from constructor
	 * @throws IOException
	 */
	public static NativeImage wrapImageFromMemory(INativeMemory nm) throws IOException
	{
		long fsize=nm.getSize();
		try
		{
			if(fsize>headerSize)
			{
				ByteBuffer bb=nm.getJavaAccessor();
				check(bb.get(), 'Q');
				check(bb.get(), 'I');
				check(bb.get(), 'M');
				check(bb.get(), 'G');
				IntBuffer ib=bb.asIntBuffer();
				int w=ib.get();
				int h=ib.get();
				SizeInt size=new SizeInt(w, h);
				int coOrdinal=ib.get();
				int afOrdinal=ib.get();
				int transparentOrOpaqueMask=ib.get();

				ENativeImageComponentOrder co=ENativeImageComponentOrder.values()[coOrdinal];
				ENativeImageAlphaStorageFormat af=ENativeImageAlphaStorageFormat.values()[afOrdinal];
				if(co.getNCHannels()*size.getNumberOfPixels()+headerSize!=fsize)
				{
					throw new IOException("File length does not match width, height and component order of image (" +
							w+"x"+h+"("+co+") - "+fsize+
							"). Image file is corrupted");
				}
				INativeMemory imMem=new WrappedJavaNativeMemory(nm, headerSize, (int)fsize);
//				nm.decrementReferenceCounter();
				NativeImage ret=new NativeImage(imMem, new SizeInt(w, h), co, 1);
				ret.setAlphaStorageFormat(af);
				ret.transparentOrOpaqueMask=transparentOrOpaqueMask;

				return ret;
			}
			else
			{
				throw new IOException("File length is smaller than "+headerSize+" bytes. Image file is corrupted");
			}
		}finally
		{
//			nm.decrementReferenceCounter();
		}
	}
	private static void check(byte b, char c) throws IOException {
		if(((char)b)!=c)
		{
			throw new IOException("File magic number does not match 'QIMG'. Image file is corrupted");
		}
	}
	
	/**
	 * Undo alpha premultiplication on a native image.
	 * 
	 * Handles only RGBA images.
	 * 
	 * @param ret
	 */
	public static void undoPreMultipliedAlpha(NativeImage ret) {
		if(ENativeImageComponentOrder.ARGB!=ret.getComponentOrder())
		{
			throw new RuntimeException("Image format not handled: "+ret.getComponentOrder());
		}
		ByteBuffer bb=ret.getBuffer().getJavaAccessor();
		bb.clear();
		int height=ret.getSize().getHeight();
		int width=ret.getSize().getWidth();
		int pos=0;
		for(int y=0;y<height;++y)
		{
			for(int x=0;x<width;++x)
			{
				byte r=bb.get();
				byte g=bb.get();
				byte b=bb.get();
				byte a=bb.get();
				
				
				float mul= (a==0)? 1.0f : (1.0f/((float)(a&0xFF)/255));
				r=corrigate(r, mul);
				g=corrigate(g, mul);
				b=corrigate(b, mul);
				
				bb.position(pos);
				bb.put(r);
				bb.put(g);
				bb.put(b);
				bb.put(a);
				pos+=4;
			}
		}
		
		bb.clear();
	}

	private static byte corrigate(byte r, float mul) {
		int v=(int)(mul*(r&0xFF));
		if(v>255) v=255;
		if(v<0) v=0;
		return (byte)v;
	}

	/**
	 * Compare two images if they are equal or not.
	 * @param imSrc first image to be compared
	 * @param imOut second image to be compared
	 * @return null if equal or error description if not equal.
	 */
	public static String isEqual(NativeImage imSrc, NativeImage imOut) {
		if(!imSrc.getComponentOrder().equals(imOut.getComponentOrder()))
		{
			return "Component order is different";
		}
		if(!imSrc.getAlphaStorageFormat().equals(imOut.getAlphaStorageFormat()))
		{
			return "Alpha storage format is different";
		}
		if(!imSrc.getSize().equals(imOut.getSize()))
		{
			return "Size is different: "+imSrc.getSize()+" "+imOut.getSize();
		}
		SizeInt s=imSrc.getSize();
		int nc=imSrc.getComponentOrder().getNCHannels();
		for(int y=0;y<s.getHeight();++y)
		{
			for(int x=0;x<s.getWidth();++x)
			{
				for(int c=0;c<nc;++c)
				{
					int src=imSrc.getChannel(x, y, c);
					int dst=imOut.getChannel(x, y, c);
					if(src!=dst)
					{
						int [] srcs = new int[nc]; 
						int [] dsts = new int[nc]; 
						for(int c2=0;c2<nc;++c2){
							srcs[c2]=imSrc.getChannel(x, y, c2);
							dsts[c2]=imOut.getChannel(x, y, c2);
						}
						return "First different pixel: coordinate: ["+x+" "+y+"] channel: "+c+". Expected pixel color "+Arrays.toString(srcs)+", generated pixel color "+Arrays.toString(dsts);
					}
				}
				
			}
		}
		return null;
	}
	

	public static byte[] imageToMd5(NativeImage nim) throws NoSuchAlgorithmException {
		ByteBuffer bb=nim.getBuffer().getJavaAccessor().duplicate();
		bb.position(0);
		bb.limit(bb.capacity());
		MessageDigest md=MessageDigest.getInstance("MD5");
//		md.update(nim.getAlphaStorageFormat().ordinal());
		md.update(bb);
		return md.digest();
	}
	/**
	 * Generate a hash code of the imge using the relatively cheap
	 * algorithm specified in List.hashCode.
	 * @param nim
	 * @return
	 */
	public static int imageToCheapHash(NativeImage nim)
	{
		int hashCode=0;
		int w=nim.getWidth();
		int h=nim.getHeight();
		hashCode=31*hashCode+w;
		hashCode=31*hashCode+h;
		hashCode=31*hashCode+nim.getComponentOrder().ordinal();
		hashCode=31*hashCode+nim.getAlphaStorageFormat().ordinal();
		int step=nim.getStep();
		int nByteInStep=w*nim.getnChannels();
		ByteBuffer bb=nim.getBuffer().getJavaAccessor().duplicate();
		bb.position(0);
		for(int i=0;i<h;++i)
		{
			int ptr=step*i;
			bb.limit(ptr+step);
			bb.position(ptr);
			for(int j=0;j<nByteInStep;++j)
			{
				hashCode=31*hashCode+bb.get();
			}
		}
		return hashCode;
	}
	/**
	 * Copy data from a native memory byte buffer to Java byte array.
	 * @param nm
	 * @return
	 */
	public static byte[] nativeMemoryToBytes(INativeMemory nm) {
		ByteBuffer bb=nm.getJavaAccessor().duplicate();
		bb.position(0);
		bb.limit(bb.capacity());
		byte[] ret=new byte[bb.capacity()];
		bb.get(ret);
		return ret;
	}
	
	/**
	 * Convert the type of the source image to the required type.
	 * In case the source image already has the target type then return it.
	 * @param image
	 * @return
	 */
	public static NativeImage convertType(NativeImage image,
			INativeMemoryAllocator allocator, ENativeImageComponentOrder order)
	{
		if (image.getComponentOrder() != order){
			NativeImage n2 = NativeImage.create(image.getSize(),order, allocator);
			n2.copyFromSource(image, 0, 0);
			image.dispose();
			image = n2;
		}
		return image;
	}

	/**
	 * Saves specified native image as a tiff file. The native image data should
	 * have {@link ENativeImageComponentOrder#RGB} pixel order. This utility
	 * method converts it to RGB format if needed, but in a non-efficient way.
	 * 
	 * @param im
	 *            The image to save
	 * @param outputFile
	 *            the target file
	 * @throws IOException
	 */
	public static void saveImageToTiff(NativeImage im, File outputFile)throws IOException {
		ENativeImageComponentOrder requiredPixelOrder = ENativeImageComponentOrder.RGB;
		if(im.getComponentOrder() != requiredPixelOrder){
			NativeImage n2 = NativeImage.create(im.getSize(),requiredPixelOrder, new DefaultJavaNativeMemoryAllocator());
			n2.copyFromSource(im, 0, 0);
			NativeTiffLoader.getInstance().saveImageAsTiff(n2, outputFile);
			n2.dispose();
		} else {
			NativeTiffLoader.getInstance().saveImageAsTiff(im, outputFile);
		}
	}
	
	/**
	 * Loads a {@link NativeImage} from tiff fileformat.
	 * 
	 * @param tiffFile
	 * @return
	 * @throws IOException
	 */
	public static NativeImage loadImageFromTiff(File tiffFile) throws IOException{
		return NativeTiffLoader.getInstance().loadImageFromTiff(tiffFile);
	}

	/**
	 * Interprets given byteArray as tiff file.
	 * 
	 * @param tiffFile
	 * @return
	 * @throws IOException
	 */
	public static NativeImage loadImageFromTiff(byte[] tiffFile) throws IOException{
		return NativeTiffLoader.getInstance().loadImageFromTiff(tiffFile);
	}
	
	/**
	 * Returns <code>true</code> if the specified image has at least one
	 * transparent pixel, <code>false</code> otherwise.
	 * 
	 * @param image
	 * @return
	 */
	public static boolean hasTransparentPixel(NativeImage image) {
		for (int i = 0; i< image.getWidth(); i++){
			for(int j = 0; j< image.getHeight(); j++){
				if (image.getRGBA(i, j)[3] != 255){
					return true;
				}
			}
		}
		return false;
	}
	public static NativeImage loadPngAndConvert(URL url,
			INativeMemoryAllocator allocator, ENativeImageComponentOrder order) throws IOException {
		NativeImage ret=NativeLibPng.loadImage(url);
		return UtilNativeImageIo.convertType(ret, allocator, order);
	}
}
