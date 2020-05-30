package hu.qgears.images.vnc;

import java.nio.ByteBuffer;

public class RingBuffer {
	private ByteBuffer ring;
	private int ringCap;
	private volatile int writePtr;
	private volatile int readPtr;
	public RingBuffer(ByteBuffer ring) {
		super();
		this.ring = ring;
		ringCap=ring.capacity();
		ring.clear();
		ring.limit(ring.capacity());
	}
	public void write(ByteBuffer data)
	{
		if(writeAvailable()<data.remaining())
		{
			throw new RuntimeException("Buffer is full");
		}
		while(data.hasRemaining())
		{
			int wrAt=writePtr;;
			byte b=data.get();
			ring.put(wrAt, b);
			writePtr=(wrAt+1)%ringCap;
		}
	}
	public void read(ByteBuffer data)
	{
		if(readAvailable()<data.remaining())
		{
			throw new RuntimeException("Data not available");
		}
		while(data.hasRemaining())
		{
			data.put(ring.get(readPtr));
			readPtr=(readPtr+1)%ringCap;
		}
	}
	public int readAvailable()
	{
		return (writePtr+ringCap-readPtr)%ringCap;
	}
	public int writeAvailable()
	{
		return ringCap-readAvailable()-1;
	}
}
