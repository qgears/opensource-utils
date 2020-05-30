package hu.qgears.images.vnc;

import java.nio.ByteBuffer;

/**
 * VNC pixel format structure.
 * Internal use only.
 * @author rizsi
 *
 */
public class PixelFormat {
	public byte bitsPerPixel;
	public byte depth;
	public byte bigEndianFlag;
	public byte trueColorFlag;
	public short redMax;
	public short greenMax;
	public short blueMax;
	public byte redShift;
	public byte greenShift;
	public byte blueShift;
	public int readRedShift;
	public int readGreenShift;
	public int readBlueShift;
	public byte padding0;
	public byte padding1;
	public byte padding2;
	@Override
	public String toString() {
		return "bpp: "+bitsPerPixel+" depth: "+depth+" bigendian: "+bigEndianFlag+" truecolor: "+trueColorFlag+" redmax: "+redMax+" redshift: "+redShift+"greenmax: "+greenMax+" greenshift: "+greenShift+"bluemax: "+blueMax+ " blueshift: "+blueShift;
	}

	public void init() {
		bitsPerPixel = 32;
		depth = 24;
		bigEndianFlag = 0;
		trueColorFlag = 1;
		redMax = 255;
		blueMax = 255;
		greenMax = 255;
		redShift = 0;
		greenShift = 8;
		blueShift = 16;
		padding0 = 0;
		padding1 = 0;
		padding2 = 0;
	}
	public void parse(ByteBuffer bb)
	{
		bitsPerPixel=bb.get();
		depth=bb.get();
		bigEndianFlag=bb.get();
		trueColorFlag=bb.get();
		redMax=bb.getShort();
		greenMax=bb.getShort();
		blueMax=bb.getShort();
		redShift=bb.get();
		greenShift=bb.get();
		blueShift=bb.get();
		padding0=bb.get();
		padding1=bb.get();
		padding2=bb.get();
	}
	public void check()
	{
		check("depth", depth, 24);
		check("bitsPerPixel", bitsPerPixel, 32);
		check("trueColorFlag", trueColorFlag, 1);
	}
	private void check(String string, byte depth2, int i) {
		if(depth2!=i)
		{
			throw new RuntimeException(""+string+" must be "+i+" (and is: "+depth2+")");
		}
	}
	public void serialize(ByteBuffer bb) {
		bb.put(bitsPerPixel);
		bb.put(depth);
		bb.put(bigEndianFlag);
		bb.put(trueColorFlag);
		bb.putShort(redMax);
		bb.putShort(greenMax);
		bb.putShort(blueMax);
		bb.put(redShift);
		bb.put(greenShift);
		bb.put(blueShift);
		bb.put(padding0);
		bb.put(padding1);
		bb.put(padding2);
	}
	/**
	 * Throw exception if can not handle this on server side.
	 * Fill the shift and mask values
	 */
	public void validate()
	{
		if(bigEndianFlag!=0)
		{
			throw new RuntimeException("Unsupported endianness: "+bigEndianFlag);
		}
		if(bitsPerPixel==8 || bitsPerPixel==16 || bitsPerPixel==24 || bitsPerPixel==32)
		{
			readRedShift=16+8-getNBit(redMax);
			readGreenShift=8+8-getNBit(greenMax);
			readBlueShift=0+8-getNBit(blueMax);
		}else
		{
			throw new RuntimeException("Unsupported bits per pixel: "+bitsPerPixel);
		}
	}
	private int getNBit(short m) {
		int v=2;
		for(int i=1;i<9;++i)
		{
			if(m+1==v)
			{
				return i;
			}
			v*=2;
		}
		throw new RuntimeException();
	}

	public void encodeStrip(ByteBuffer sendByteBuffer, int step, ByteBuffer src, int x, int y, int w) {
		int at=step*y+x*4;
		int sendStep=bitsPerPixel/8;
		int sendPosStart=sendByteBuffer.position();
		int redShiftX=redShift+32-depth;
		int greenShiftX=greenShift+32-depth;
		int blueShiftX=blueShift+32-depth;
		for(int i=0;i<w;++i)
		{
			int data=src.getInt(at);
			int r=data;
			int g=data;
			int b=data;
			r>>=readRedShift;
			g>>=readGreenShift;
			b>>=readBlueShift;
			r&=redMax;
			g&=greenMax;
			b&=blueMax;
			r<<=redShiftX;
			g<<=greenShiftX;
			b<<=blueShiftX;
			int val=r|g|b;
			sendByteBuffer.putInt(val);
			sendByteBuffer.position(sendPosStart+sendStep*(i+1));
			at+=4;
		}
	}
}
