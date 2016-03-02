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
}