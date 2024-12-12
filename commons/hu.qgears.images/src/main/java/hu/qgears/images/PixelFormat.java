package hu.qgears.images;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import hu.qgears.images.text.RGBAColor;

public class PixelFormat {
	/**
	 * Number of bits for red channel.
	 */
	public final byte rBits;
	/**
	 * Number of bits to shift left the red channel.
	 */
	public final byte rShift;
	/**
	 * Number of bits for green channel.
	 */
	public final byte gBits;
	public final byte gShift;
	public final byte bBits;
	public final byte bShift;
	public final byte aBits;
	public final byte aShift;
	/**
	 * Number of all bits to represent a single pixel.
	 */
	public final byte allBits;
	/**
	 * When the value is prepared in a long value then the result is stored in this
	 * byte order. (In case of byte aligned channels the same format has two different representations.)
	 */
	public final ByteOrder byteOrder;
	public PixelFormat(int rBits, int rShift, int gBits, int gShift, int bBits, int bShift, int aBits,
			int aShift, int allBits, ByteOrder byteOrder) {
		super();
		this.rBits = (byte)rBits;
		this.rShift = (byte)rShift;
		this.gBits = (byte)gBits;
		this.gShift = (byte)gShift;
		this.bBits = (byte)bBits;
		this.bShift = (byte)bShift;
		this.aBits = (byte)aBits;
		this.aShift = (byte)aShift;
		this.allBits = (byte)allBits;
		this.byteOrder = byteOrder;
	}
	public byte[] colorToPattern(RGBAColor color) {
		long value=0;
		if(rBits>0)
		{
			value|=((long)color.r)<<rShift;
		}
		if(gBits>0)
		{
			value|=((long)color.g)<<gShift;
		}
		if(bBits>0)
		{
			value|=((long)color.b)<<bShift;
		}
		if(aBits>0)
		{
			value|=((long)color.a)<<aShift;
		}
		byte[] arr=new byte[8];
		int nByte=getBytesPerPixel();
		ByteBuffer.wrap(arr).order(byteOrder).putLong(value);
		if(byteOrder==ByteOrder.LITTLE_ENDIAN) {
			return Arrays.copyOfRange(arr, 0, nByte);
		}
		if(byteOrder==ByteOrder.BIG_ENDIAN) {
			return Arrays.copyOfRange(arr, 8-nByte, 8);
		}
		throw new RuntimeException("Internal error unhandled byte order: "+byteOrder);
	}
	public int getBytesPerPixel() {
		return (allBits+7)/8;
	}
}
