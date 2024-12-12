package hu.qgears.images.libpng;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.images.ENativeImageAlphaStorageFormat;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

public class NativeLibPng extends NativeLibPngConnector
{
	private static final Logger LOG = Logger.getLogger(NativeLibPng.class);
	
	public NativeLibPng()
	{
		try {
			NativeLibPngManager.getInstance();
		} catch (Exception e) {
			LOG.error("Cannot initialize LibPng connector",e);
		}
	}
	public NativeImage loadImage(byte[] imageData, INativeMemoryAllocator allocator, int alignment)
	{
		try(INativeMemory nm=DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(imageData.length))
		{
			ByteBuffer data=nm.getJavaAccessor();
			data.put(imageData);
			return loadImage(data, allocator, alignment);
		}
	}
	public NativeImage loadImage(ByteBuffer imageData, INativeMemoryAllocator allocator, int alignment)
	{
		if(!imageData.isDirect())
		{
			throw new NativeLibPngException("PNG image data must be a direct byte buffer");
		}
		try
		{
			beginLoad(imageData);
			int w=getWidth();
			int h=getHeight();
			int nChannel=getNumberOfChannels();
			ENativeImageComponentOrder componentOrder;
			switch (nChannel) {
			case 1:
				componentOrder=ENativeImageComponentOrder.MONO;
				break;
			case 3:
				componentOrder=ENativeImageComponentOrder.RGB;
				break;
			case 4:
				componentOrder=ENativeImageComponentOrder.RGBA;
				break;
			default:
				throw new NativeLibPngException("Invalid number of channels: "+nChannel);
			}
			int rowBytes=getRowBytes();
			int mod=rowBytes%alignment;
			if(mod!=0)
			{
				rowBytes+=alignment-mod;
			}
			INativeMemory mem=allocator.allocateNativeMemory(rowBytes*h, 16);
			try
			{
				loadImage(mem.getJavaAccessor(), rowBytes);
				mem.incrementReferenceCounter();
				NativeImage ret=new NativeImage(mem, new SizeInt(w, h), componentOrder, alignment);
				return ret;
			}finally
			{
				mem.decrementReferenceCounter();
			}
		}finally
		{
			closeLoad();
		}
	}
	/**
	 * Save image to PNG format.
	 * @param im image to be saved
	 * @param nativeMemoryAllocator memory allocator that is going to be used to allocate memory for the result
	 * @return the allocated native memory that contains the image data. The allocated size will be exactly the size of the PNG file.
	 */
	public INativeMemory saveImage(NativeImage im,
			INativeMemoryAllocator nativeMemoryAllocator) {
		ENativeImageComponentOrder co=im.getComponentOrder();
		ENativeImageAlphaStorageFormat asf=im.getAlphaStorageFormat();
		boolean swapAlpha=false;
		boolean swapBGR=false;
		boolean premultipliedAlpha = false;
		switch (co) {
		case MONO:
		case RGB:
		case RGBA:
			break;
		case ARGB:
			swapAlpha=true;
			break;
		case ABGR:
			swapAlpha=true;
			swapBGR=true;
			break;
		case BGR:
			swapBGR=true;
			break;
		case BGRA:
			swapBGR=true;
			break;
		default:
			throw new NativeLibPngException("Unknown image pixel format: "+co);
		}
		switch (asf) {
		case normal:
			break;
		case premultiplied:
			premultipliedAlpha = true;
			break;
		default:
			throw new NativeLibPngException("Unknown alpha format: "+asf);
		}
		INativeMemory buf=im.getBuffer();
		if(!buf.getJavaAccessor().isDirect())
		{
			throw new NativeLibPngException("image data must be a direct byte buffer");
		}
		try
		{
			SizeInt s=im.getSize();
			ByteBuffer bb=buf.getJavaAccessor();
			beginSave(s.getWidth(), s.getHeight(), im.getStep(),
					im.getnChannels(),
					swapAlpha,
					swapBGR,
					premultipliedAlpha,
					bb);
			int l=getFileSize();
			INativeMemory ret=nativeMemoryAllocator.allocateNativeMemory(l);
			try
			{
				saveImage(ret.getJavaAccessor());
				ret.incrementReferenceCounter();
			}finally
			{
				ret.decrementReferenceCounter();
			}
			return ret;
		}finally
		{
			closeSave();
		}
	}
	public void saveImage(NativeImage im, File out) throws IOException {
		INativeMemory mem=saveImage(im, DefaultJavaNativeMemoryAllocator.getInstance());
		UtilFile.saveAsFile(out, mem.getJavaAccessor());
		mem.decrementReferenceCounter();
	}
	/**
	 * Load image from stream using default native memory allocator.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static NativeImage loadImage(InputStream is) throws IOException {
		return new NativeLibPng().loadImage(UtilFile.loadFile(is), DefaultJavaNativeMemoryAllocator.getInstance(),
				DefaultJavaNativeMemoryAllocator.getInstance().getDefaultAlignment());
	}
	/**
	 * Load image from URL using default native memory allocator.
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static NativeImage loadImage(URL resource) throws IOException {
		InputStream is=resource.openStream();
		try
		{
			return loadImage(is);
		}finally
		{
			is.close();
		}
	}
	/**
	 * Load image from file using default native memory allocator.
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static NativeImage loadImage(File in) throws IOException {
		return new NativeLibPng().loadImage(UtilFile.loadFile(in), DefaultJavaNativeMemoryAllocator.getInstance(),
				DefaultJavaNativeMemoryAllocator.getInstance().getDefaultAlignment());
	}
	/**
	 * Load image from byte array containing png data using default native memory allocator.
	 * @param data
	 * @return
	 */
	public static NativeImage loadImage(byte[] data) {
		return new NativeLibPng().loadImage(data, DefaultJavaNativeMemoryAllocator.getInstance(),
				DefaultJavaNativeMemoryAllocator.getInstance().getDefaultAlignment());
	}
}
