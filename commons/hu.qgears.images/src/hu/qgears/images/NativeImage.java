package hu.qgears.images;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.images.text.RGBAColor;

import java.nio.ByteBuffer;


/**
 * Image in native memory that can be accessed as:
 *  * OpenGL texture
 *  * source for native filters
 *  * target as native webcam implementation.
 * @author rizsi
 *
 */
public class NativeImage extends AbstractReferenceCountedDisposeable
{
	/**
	 * The transparent marker bit mask in the returned value of getTransparentOrOpaqueMask() method.
	 * The returned value means transparent when getTransparentOrOpaqueMask()&transparentBit!=0
	 */
	public static final int transparentBit=1;
	/**
	 * The opaque marker bit mask in the returned value of getTransparentOrOpaqueMask() method.
	 * The returned value means opaque when getTransparentOrOpaqueMask()&opaqueBit!=0
	 */
	public static final int opaqueBit=2;
	
	private INativeMemory buffer;
	private ByteBuffer nbuffer;
	private SizeInt size;
	private ENativeImageComponentOrder componentOrder;
	private int nChannels;
	private int width;
	private ENativeImageAlphaStorageFormat alphaStorageFormat=ENativeImageAlphaStorageFormat.normal;
	
	/** The alignment of rows to memory */
	private int alignment;
	/** The length of one row and padding (required by alignment) in memory */
	private int step;
	/**
	 * Stores cached transparency and opacity information. BIG FAT WARNING: the
	 * value of this variable may be considered correct only if the image has been
	 * loaded using {@link UtilNativeImageIo#loadImageFromFile(java.io.File)} or
	 * {@link UtilNativeImageIo#loadImageFromFile(INativeMemory)}, or, if the
	 * image has been created another way, {@link #getTransparentOrOpaqueMask()} 
	 * is called previously.
	 * In case the image is modified since calculation of this flag this flag will be incorrect!
	 */
	private int transparentOrOpaqueMask = -1;
	/** 
	 * The default alignment of native image rows to memory.
	 * this means that all row start an an address that: ptrRow%defaultAlignment=0
	 */
	public static int defaultAlignment=1;
	/**
	 * The alignment that allows compatibility with openCV.
	 */
	public static int compatibleAlignment=4;
	/**
	 * Create a native image from existing data.
	 * @param buffer see getBuffer()
	 * @param size see getSize()
	 * @param componentOrder see getComponentOrder()
	 * @param alignemnt see getAlignment()
	 */
	public NativeImage(INativeMemory buffer, SizeInt size, ENativeImageComponentOrder componentOrder, int alignment) {
		super();
		init(buffer, size, componentOrder, alignment);
	}
	private void init(INativeMemory buffer, SizeInt size, ENativeImageComponentOrder componentOrder, int alignment)
	{
		this.buffer = buffer;
		this.size=size;
		this.componentOrder=componentOrder;
		this.nChannels=componentOrder.getNCHannels();
		this.width=size.getWidth();
		this.alignment=alignment;
		this.step=getStep(this.width, componentOrder, alignment);
		this.nbuffer=buffer.getJavaAccessor();
	}
	/**
	 * Get the memory buffer that contains image pixel data.
	 * @return
	 */
	public INativeMemory getBuffer() {
		return buffer;
	}
	/**
	 * Get the width of the image in pixels.
	 * @return
	 */
	public int getWidth() {
		return size.getWidth();
	}
	/**
	 * Get the height of the image in pixels.
	 * @return
	 */
	public int getHeight() {
		return size.getHeight();
	}
	/**
	 * Get the pixel data storage format of the image.
	 * @return
	 */
	public ENativeImageComponentOrder getComponentOrder() {
		return componentOrder;
	}
	/**
	 * Get the size of the image in pixels.
	 * @return
	 */
	public SizeInt getSize() {
		return size;
	}
	/**
	 * Get the image pixel data of the given pixel and channel.
	 * @param x
	 * @param y
	 * @param channel
	 * @return pixel data value of the channel
	 */
	public byte getChannel(int x, int y, int channel)
	{
		return nbuffer.get(y*step+x*nChannels+channel);
	}
	/**
	 * Set the image pixel data of the given channel.
	 * @param x
	 * @param y
	 * @param channel
	 * @param value
	 * @return
	 */
	public NativeImage setChannel(int x, int y, int channel, byte value)
	{
		nbuffer.put(y*step+x*nChannels+channel, value);
		return this;
	}
	/**
	 * Create a native image that has the specified size, component order 
	 * and alignment.
	 * @param size
	 * @param componentOrder
	 * @param alignment
	 * @param allocator memory allocator to be used to allocate space for image.
	 * @return
	 */
	public static NativeImage create(
			SizeInt size,
			ENativeImageComponentOrder componentOrder,
			int alignment, INativeMemoryAllocator allocator)
	{
		int step=getStep(size.getWidth(), componentOrder, alignment);
		INativeMemory buffer=allocator.allocateNativeMemory(step*size.getHeight(), alignment);
		return new NativeImage(buffer, size, componentOrder, alignment);
	}
	/**
	 * Create a native image that has the specified size, component order 
	 * and alignment.
	 * @param size
	 * @param componentOrder
	 * @param allocator memory allocator to be used to allocate space for image.
	 * @return
	 */
	public static NativeImage create(
			SizeInt size,
			ENativeImageComponentOrder componentOrder, INativeMemoryAllocator allocator)
	{
		int alignment=allocator.getDefaultAlignment();
		int step=getStep(size.getWidth(), componentOrder, alignment);
		INativeMemory buffer=allocator.allocateNativeMemory(step*size.getHeight());
		return new NativeImage(buffer, size, componentOrder, alignment);
	}
	/**
	 * Get the length of a line of the image data in bytes.
	 * @param width width of the image in pixels
	 * @param co pixel data format
	 * @param alignment storage alignment of the image.
	 * @return
	 */
	public static int getStep(int width, ENativeImageComponentOrder co, int alignment)
	{
		int step=width*co.getNCHannels();
		int mod=step% alignment;
		if(mod>0)
		{
			step+=alignment-mod;
		}
		return step;
	}
	/**
	 * Copy the rectangular area from the source image into this image.
	 * 
	 * Target is this image at 0,0
	 * @param src
	 * @param x the left corner of source
	 * @param y the top corner of source
	 */
	public void copyFromSource(NativeImage src, int x, int y)
	{
		copyFromSource(src, x, y, 0, 0);
	}
	/**
	 * Copy the rectangular area from the source image into this image.
	 * Target is this image at tgX, tgY
	 * @param src
	 * @param x the left corner of source
	 * @param y the top corner of source
	 * @param tgX
	 * @param tgY
	 */
	public void copyFromSource(NativeImage src, int x, int y, int tgX, int tgY)
	{
		copyFromSource(src, x, y, tgX, tgY, false);
	}
	/**
	 * Do pixel copying from a source image using pixel data conversion.
	 * @param src
	 * @param x
	 * @param y
	 * @param tgX
	 * @param tgY
	 */
	private void convertFromSource(NativeImage src, int x, int y, int tgX,
			int tgY) {
		int w=Math.min(src.getSize().getWidth()-x, getSize().getWidth()-tgX);
		int h=Math.min(src.getSize().getHeight()-y, getSize().getHeight()-tgY);
		for(int j=0;j<h;++j)
		{
			for(int i=0;i<w;++i)
			{
				int pixel=src.getPixel(i+x, j+y);
				setPixel(tgX+i, tgY+j, pixel);
			}
		}
	}

	/**
	 * Copy the rectangular area beginning from the (x,y) coordinate from source
	 * image into this image beginning with (tgX,tgY) in this image.
	 * 
	 * Width and height of the resulting image is the minimum of the two images.
	 * 
	 * @param src source {@link NativeImage}
	 * @param x the left corner of source
	 * @param y the top corner of source
	 * @param tgX the left corner of target
	 * @param tgY the top corner of target
	 * @param processFromPosition
	 *            if true, processing of data from the current position of the 
	 *            source image's buffer will be performed instead of position 0
	 */
	private void copyFromSource(NativeImage src, int x, int y, int tgX, int tgY,
			boolean processFromPosition) {
		if (!src.getComponentOrder().equals(getComponentOrder())) {
			convertFromSource(src, x, y, tgX, tgY);
			return;
		}
		int nc = src.getnChannels();
		ByteBuffer srcb = src.getBuffer().getJavaAccessor();
		ByteBuffer bb = getBuffer().getJavaAccessor();
		int w = Math.min(src.getSize().getWidth() - x, getSize().getWidth() - tgX);
		int h = Math.min(src.getSize().getHeight() - y, getSize().getHeight() - tgY);
		int stepSrc = src.getStep();
		int stepTrg = getStep();
		int pos = srcb.position();
		for (int j = 0; j < h; ++j) {
			int ptrsrc = (j + y) * stepSrc + x * nc;
			if (processFromPosition) {
				srcb.limit(pos + ptrsrc + w * nc);
				srcb.position(pos + ptrsrc);
			} else {
				srcb.limit(ptrsrc + w * nc);
				srcb.position(ptrsrc);
			}
			bb.position((j + tgY) * stepTrg + tgX * nc);
			bb.put(srcb);
		}
		srcb.position(0);
		srcb.limit(srcb.capacity());
		bb.position(0);
	}	
	/**
	 * Set pixel color in rgba encoding
	 * @param i x index of the image pixel
	 * @param j y index of the image pixel
	 * @param rgba color in rgba integer (8 bit each alpha is on LSB)
	 */
	public void setPixel(int i, int j, int rgba) {
		int nc=getnChannels();
		int pos=j*step+i*nc;
		int r=(rgba>>24)&0xFF;
		int g=(rgba>>16)&0xFF;
		int b=(rgba>>8)&0xFF;
		int a=rgba&0xFF;
		switch (componentOrder) {
		case BGR:
			nbuffer.put(pos  , (byte)b);
			nbuffer.put(pos+1, (byte)g);
			nbuffer.put(pos+2, (byte)r);
			break; 
		case BGRA:
			nbuffer.put(pos  , (byte)b);
			nbuffer.put(pos+1, (byte)g);
			nbuffer.put(pos+2, (byte)r);
			nbuffer.put(pos+3, (byte)a);
			break;
		case RGB:
			nbuffer.put(pos  , (byte)r);
			nbuffer.put(pos+1, (byte)g);
			nbuffer.put(pos+2, (byte)b);
			break;
		case RGBA:
			nbuffer.put(pos  , (byte)r);
			nbuffer.put(pos+1,(byte)g);
			nbuffer.put(pos+2, (byte)b);
			nbuffer.put(pos+3, (byte)a);
			break;
		case MONO:
			nbuffer.put(pos, (byte)((b+g+r)/3));
			break;
		default:
			throw new RuntimeException("Unknown component order: "+componentOrder);
		}
	}
	/**
	 * Get pixel color in RGBA encoding.
	 * @param i
	 * @param j
	 * @return
	 */
	public int getPixel(int i, int j) {
		int nc=getnChannels();
		int pos=j*step+i*nc;
		int b;
		int g;
		int r;
		int a;
		switch (componentOrder) {
		case BGR:
			b=nbuffer.get(pos);
			g=nbuffer.get(pos+1);
			r=nbuffer.get(pos+2);
			a=255;
			break; 
		case BGRA:
			b=nbuffer.get(pos);
			g=nbuffer.get(pos+1);
			r=nbuffer.get(pos+2);
			a=nbuffer.get(pos+3);
			break;
		case RGB:
			r=nbuffer.get(pos);
			g=nbuffer.get(pos+1);
			b=nbuffer.get(pos+2);
			a=255;
			break;
		case RGBA:
			r=nbuffer.get(pos);
			g=nbuffer.get(pos+1);
			b=nbuffer.get(pos+2);
			a=nbuffer.get(pos+3);
			break;
		case ARGB:
			b=nbuffer.get(pos);
			g=nbuffer.get(pos+1);
			r=nbuffer.get(pos+2);
			a=nbuffer.get(pos+3);
			break;
		case MONO:
			g = nbuffer.get(pos);
			r = g;
			b = g;
			a=255;
			break;
		default:
			throw new RuntimeException("Unknown component order: "+componentOrder);
		}
		switch (alphaStorageFormat) {
		case normal:
			//nothing to do
			break;
		case premultiplied: {
			float f;
			if (a == 0) {
				f = 0;
			} else {
				a = a & 0xFF;
				f = 255f / a;
				r = clampMul(f, r);
				g = clampMul(f, g);
				b = clampMul(f, b);
			}
			break;
		}
		default:
			//nothing to do
			break;
		}
		return ((r<<24)&0xFF000000)+((g<<16)&0x00FF0000)+((b<<8)&0x0000FF00)+(a&0xFF);
	}
	private static final int clampMul(float f, int r) {
		float ret=f*(0xFF&r);
		if (ret > 255) {
			ret = 255;
		}
		if (ret < 0) {
			ret = 0;
		}
		return (int)ret;
	}
	/**
	 * Get the number of channels of the image format.
	 * @return
	 */
	public int getnChannels() {
		return nChannels;
	}
	/**
	 * Get the storage alignment of image data.
	 * Data of all lines are aligned to addresses with this alignment.
	 * Some image manipulation libraries require that lines of data is aligned in memory.
	 * @return 1, 2 and 4 are common values
	 */
	public int getAlignment() {
		return alignment;
	}
	/**
	 * Get the length of a stored row in bytes.
	 * @return width*bytesperpixel+alignmentIfRequired
	 */
	public int getStep() {
		return step;
	}
	/**
	 * The size of memory buffer area holding the image.
	 * It is: step*height
	 * (step is nChannel*width+alignmentPadding
	 * where aligmentPadding depends on alignment and width values.
	 * See implementation of getStep())
	 * @return
	 */
	public int getBufferSize()
	{
		return step*size.getHeight();
	}
	/**
	 * Create a copy of this image that is:
	 *  * allocated using the given allocator
	 *  * aligned to be compatible with openCV
	 * @param allocator used to allocate imagebuffer memory
	 * @return
	 */
	public NativeImage createCopy(INativeMemoryAllocator allocator) {
		NativeImage ret=create(getSize(), getComponentOrder(), compatibleAlignment, allocator);
		ret.copyFromSource(this, 0, 0);
		return ret;
	}
	@Override
	protected void singleDispose() {
		if(buffer!=null)
		{
			buffer.decrementReferenceCounter();
			buffer=null;
		}
		nbuffer=null;
	}
	/**
	 * Get the alpha storage format of the image. See {@link ENativeImageAlphaStorageFormat}
	 * @return
	 */
	public ENativeImageAlphaStorageFormat getAlphaStorageFormat() {
		return alphaStorageFormat;
	}
	public void setAlphaStorageFormat(
			ENativeImageAlphaStorageFormat alphaStorageFormat) {
		this.alphaStorageFormat = alphaStorageFormat;
	}
	/**
	 * Set the alpha value of all pixels to this value.
	 * @param b
	 */
	public void clearAlpha(byte b) {
		int channel=componentOrder.getAlphaChannel();
		if(channel>=0)
		{
			int height=size.getHeight();
			for(int y=0;y<height;++y)
			{
				for(int x=0;x<width;++x)
				{
					setChannel(x, y, channel, b);
				}
			}
		}
	}
	/**
	 * Flip the image data in the y coordinate.
	 */
	public void flipY()
	{
		int height=size.getHeight();
		int n=height/2;
		for(int y=0;y<n;++y)
		{
			for(int x=0;x<width;++x)
			{
				int y2=height-y-1;
				int rgba=getPixel(x, y);
				int rgba2=getPixel(x, y2);
				setPixel(x, y, rgba2);
				setPixel(x, y2, rgba);
			}
		}
	}

	/**
	 * Lazy init and return the transparent or opaque mask of the image.
	 * This method finds transparency only in 4 byte image formats. In other cases the returned value will be 0.
	 * In case of non alpha channel formats the returned value will be {@link opaqueBit}
	 * The first time when this test is executed it will run slow because it reads the whole image data.
	 * @return transparency and opacity mask see opaqueBit and transparentBit constants.
	 */
	public int getTransparentOrOpaqueMask() {
		if (this.transparentOrOpaqueMask == -1) {		
			final int alphaChannelIdx = getComponentOrder().getAlphaChannel();
			
			if(alphaChannelIdx!=-1)
			{
				if(getComponentOrder().getNCHannels()==4
					&& getAlignment()<=4)
				{
					int oMask=0xff<<(3-alphaChannelIdx)*8;
					ByteBuffer bb=getBuffer().getJavaAccessor();
					int c=bb.capacity()/4;
					bb.clear();
					boolean t=true;
					boolean o=true;
					for(int i=0;i<c;++i)
					{
						int v=bb.getInt();
						t&=v==0;
						o&=((v&oMask)==oMask);
					}
					
					bb.clear();
					transparentOrOpaqueMask = (t?transparentBit:0)+(o?opaqueBit:0);
				}else
				{
					transparentOrOpaqueMask=0;
				}
			} else {
				transparentOrOpaqueMask = opaqueBit;
			}
		}
		
		return transparentOrOpaqueMask;
	}

	
	/**
	 * Returns the color of specified pixel.
	 * 
	 * @param x X coordinate of pixel
	 * @param y Y coordinate of pixel
	 * @return The pixel color as a {@link RGBAColor}
	 * 
	 * @since 3.0
	 */
	public RGBAColor getRGBA(int x, int y){
		int pixel = getPixel(x, y);
		return new RGBAColor(
				(pixel  >> 24) & 0xFF,
				(pixel >> 16) & 0xFF,
				(pixel >> 8) & 0xFF,
				pixel  & 0xFF
		);
	}
	/**
	 * This method is only to be used by UtilNativeImageIo class!
	 * @param transparentOrOpaqueMask
	 */
	public void setTransparentOrOpaqueMask(int transparentOrOpaqueMask) {
		this.transparentOrOpaqueMask=transparentOrOpaqueMask;
	}
	/**
	 * Set the given pixel to the given color.
	 * @param x
	 * @param y
	 * @param c
	 * 
	 * @since 5.0
	 */
	public void setPixel(int x, int y, RGBAColor c) {
		setPixel(x, y, c.r<<24|c.g<<16|c.b<<8|c.a);
	}
}
