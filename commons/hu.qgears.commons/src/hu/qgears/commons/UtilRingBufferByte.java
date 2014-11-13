package hu.qgears.commons;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Ringbuffer implementation based on a byte array.
 * 
 * Allows two threads to access the same buffer - one as feeder
 * other as consumer.
 * 
 * Methods are not synchronized. The threads of feeder and consumer must not be
 * changed.
 * 
 * @author rizsi
 */
public class UtilRingBufferByte {
	private byte data[];
	private volatile int writePtr=0;
	private volatile int readPtr=0;
	/**
	 * Create a new ringbuffer with length number of bytes in the ring
	 * @param length the size of the ringbuffer in bytes
	 */
	public UtilRingBufferByte(int length)
	{
		data=new byte[length];
	}
	/**
	 * Non blocking write to the ring.
	 * Write a single byte of data into the ringbuffer.
	 * The write never blocks. In case of overflow false will be returned
	 * @param b
	 * @return true if byte could be written without overflow
	 */
	public boolean writeData(byte b)
	{
		int nextPtr=(writePtr+1)%data.length;
		if(nextPtr!=readPtr)
		{
			data[writePtr]=b;
			writePtr=nextPtr;
			return true;
		}
		return false;
	}
	/**
	 * Non-blocking read from the ring
	 * @return -1 means no data available
	 */
	public int readData()
	{
		if(writePtr!=readPtr)
		{
			byte ret=data[readPtr];
			readPtr=(readPtr+1)%data.length;
			return ret;
		}
		return -1;
	}
	/**
	 * Get an outputstream interface to write into this ringbuffer.
	 * Writes never block the caller.
	 * In case of ringbuffer overflow the bytes written will be lost.
	 * @return new Outputstream obejct that writes to this ringbuffer.
	 */
	public OutputStream createOutputStream() {
		return new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				writeData((byte)b);
			}
		};
	}
}
