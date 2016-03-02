package hu.qgears.images.devil;

import hu.qgears.commons.IDisposeable;
import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

import java.io.File;
import java.nio.ByteBuffer;


public class NativeDevIL implements IDisposeable {
	private long ptr;
	private int width;
	private int height;
	protected NativeDevIL() {
		//default ctor
	}
	protected native void initDevIL();
	private native int bindImage();
	private native int getTypeId(String ext);
	private native int loadImage(ByteBuffer content, int typeId);
	private native ByteBuffer convertImage();
	private native int getWidthPrivate();
	private native int getHeightPrivate();
	protected native void init();
	private ByteBuffer decoded;
	public void load(byte[] content, String ext) {
		ByteBuffer bb=ByteBuffer.allocateDirect(content.length);
		bb.put(content);
		bb.flip();
		load(bb, ext);
	}
	public void load(ByteBuffer content, String ext) {
		bindImage();
		int typeId=getTypeId(ext);
		int ret=loadImage(content, typeId);
		if(ret!=0)
		{
			throw new RuntimeException("Error decoding image: "+ret);
		}
		decoded=convertImage();
		width=getWidthPrivate();
		height=getHeightPrivate();
	}
	/**
	 * Save image into file.
	 * @param image
	 * @param componentorder
	 * @param outFile
	 */
	public void save(NativeImage image, ENativeImageComponentOrder componentorder, File outFile)
	{
		if(image.getStep()%image.getAlignment()!=0)
		{
			throw new RuntimeException("Image must be aligned to 1");
		}
		if(!ENativeImageComponentOrder.RGBA.equals(image.getComponentOrder()))
		{
			throw new RuntimeException("Component order must be "+ENativeImageComponentOrder.RGBA);
		}
		saveImage(image.getBuffer()
				.getJavaAccessor()
				, outFile.getAbsolutePath(), image.getWidth(), image.getHeight());
	}
	private native void saveImage(ByteBuffer buffer, String outFile, int width,
			int height);
	@Override
	public void dispose() {
		if(ptr!=0)
		{
			nativeDispose();
			width = 0;
			height = 0;
			decoded=null;
		}
		ptr=0;
	}
	private native void nativeDispose();
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	/**
	 * Get the devIL buffer of the image.
	 * This memory area is managed by the devIL library and
	 * is freed with the devIL object freed.
	 * @return
	 */
	private ByteBuffer getBuffer()
	{
		return decoded;
	}
	/**
	 * Copy the loaded image into a Java managed direct byte buffer.
	 * The alignment of the returned image is compatible with openCV
	 * @return
	 */
	public NativeImage copyBuffer(INativeMemoryAllocator allocator)
	{
		ByteBuffer devilBuffer = getBuffer();
		ByteBuffer buffer = ByteBuffer.allocateDirect(devilBuffer
				.capacity());
		buffer.put(devilBuffer);
		buffer.flip();
		NativeImage toCopy=
			new NativeImage(
					new DefaultJavaNativeMemory(devilBuffer), new SizeInt(width, height), ENativeImageComponentOrder.BGRA, 1);
		return toCopy.createCopy(allocator);
	}
	@Override
	public boolean isDisposed() {
		return ptr==0;
	}
}
