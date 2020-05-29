package hu.qgears.images.vnc;

import java.nio.ByteBuffer;

public class VncEvent {
	public static final int storageSize=8;
	private ByteBuffer bb=ByteBuffer.allocate(storageSize);
	public void readFrom(RingBuffer events) {
		bb.clear();
		events.read(bb);
		bb.flip();
	}
	public int getEventType()
	{
		return bb.get(0);
	}
	public boolean isPointer() {
		return bb.get(0)==5;
	}
	public int getPointerButtonMask() {
		return bb.get(1)&0xff;
	}
	public int getPointerX() {
		return bb.getShort(2)&0xffff;
	}
	public int getPointerY() {
		return bb.getShort(4)&0xffff;
	}
	public int getKey()
	{
		return bb.getInt(4);
	}
}
