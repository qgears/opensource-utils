package hu.qgears.images.swing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

/**
 * Conversion methods to and from {@link BufferedImage}.
 */
public class UtilSwing {
	private UtilSwing(){}
	/**
	 * Convert a {@link BufferedImage} to a {@link NativeImage}.
	 * Note that not all {@link BufferedImage} storage formats are handled.
	 * Note that the target {@link ENativeImageComponentOrder} is different based on the source.
	 * @param bufferedImage the {@link BufferedImage} to be converted
	 * @param allocator the allocator used to allocate buffer to store the image pixels
	 * @return the image in {@link NativeImage} format.
	 * @throws RuntimeException in case of unknown input format.
	 */
	public static NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage, 
			INativeMemoryAllocator allocator)
	{
		int w=bufferedImage.getWidth();
		int h=bufferedImage.getHeight();
		final DataBuffer bufferedImageData = bufferedImage.getRaster().getDataBuffer();
		switch(bufferedImage.getType())
		{
		case BufferedImage.TYPE_4BYTE_ABGR:
		{
			DataBufferByte bufferedImageDataByte = (DataBufferByte)bufferedImageData;
			NativeImage im=NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.RGBA,
					allocator);
			byte[] src=bufferedImageDataByte.getData();
			int l=src.length/4;
			ByteBuffer bb=im.getBuffer().getJavaAccessor();
			for(int i=0;i<l;++i)
			{
				byte r=src[i*4+3];
				byte g=src[i*4+2];
				byte b=src[i*4+1];
				byte a=src[i*4+0];
				bb.put(r);
				bb.put(g);
				bb.put(b);
				bb.put(a);
			}
			im.getBuffer().getJavaAccessor().flip();
			return im;
		}
		case BufferedImage.TYPE_3BYTE_BGR:
		{
			DataBufferByte bufferedImageDataByte = (DataBufferByte)bufferedImageData;
			NativeImage im=NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.BGR,
					allocator);
			im.getBuffer().getJavaAccessor().put(bufferedImageDataByte.getData());
			im.getBuffer().getJavaAccessor().flip();
			return im;
		}
		case BufferedImage.TYPE_INT_ARGB:
		{
			DataBufferInt bufferedImageDataByte = (DataBufferInt)bufferedImageData;
			NativeImage im=NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.RGBA,
					allocator);
			ByteBuffer byteBuffer=im.getBuffer().getJavaAccessor();
			final IntBuffer intBuffer = byteBuffer.asIntBuffer();
			int[] data=bufferedImageDataByte.getData();
			for(int i=0;i<data.length;++i)
			{
				int val=data[i];
				int a=0xFF&(val>>24);
				int rgb=val<<8;
				intBuffer.put(rgb|a);
			}
			intBuffer.flip();
			return im;
		}
		case BufferedImage.TYPE_BYTE_GRAY:
		{
			DataBufferByte bufferedImageDataByte = (DataBufferByte)bufferedImageData;
			NativeImage im=NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.MONO,
					allocator);
			ByteBuffer byteBuffer=im.getBuffer().getJavaAccessor();
			byte[] data=bufferedImageDataByte.getData();
			for(int i=0;i<data.length;++i)
			{
				byte val=data[i];
				byteBuffer.put(val);
			}
			byteBuffer.flip();
			return im;
		}
		default:
			throw new RuntimeException("Unknown buffered image type: "+bufferedImage.getType());
		}
	}
}
