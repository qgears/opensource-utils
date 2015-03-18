package hu.qgears.shm.part;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryWithRelativeAddress;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Manually grab a part of an already allocated memory.
 * Can be used to:
 *  * manually patition allocated big chunk of memory
 *  * implement allocator in Javas
 * @author rizsi
 *
 */
public class PartNativeMemory extends AbstractReferenceCountedDisposeable implements INativeMemoryWithRelativeAddress {
	INativeMemory host;
	private ByteBuffer javaAccessor;
	private long offset;
	private long size;
	private long nativePointer1;
	private long nativePointer2;
	public PartNativeMemory(INativeMemory host, long offset, long size) {
		super();
		this.host = host;
		this.offset = offset;
		this.size = size;
		PartNativeMemoryNative nat=new PartNativeMemoryNative();
		javaAccessor=nat.getByteBuffer(host.getJavaAccessor(), offset, size);
		this.javaAccessor.order(ByteOrder.nativeOrder());
		nativePointer1=nat.getNativePointer(javaAccessor, 1);
		nativePointer2=nat.getNativePointer(javaAccessor, 2);
	}

	@Override
	public ByteBuffer getJavaAccessor() {
		return javaAccessor;
	}

	@Override
	public long getNativePointer1() {
		return nativePointer1;
	}

	@Override
	public long getNativePointer2() {
		return nativePointer2;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getRelativeAddress() {
		return offset;
	}

	@Override
	public INativeMemory getHost() {
		return host;
	}

	@Override
	protected void singleDispose() {
		// Only the host is managed - nothing to do
	}

	@Override
	public long getAfterRelativeAddress() {
		return getRelativeAddress()+getSize();
	}
	/**
	 * 
	 * @param ptrPart1
	 * @param ptrPart2
	 * @param size
	 * @return buffer with native order set!
	 */
	static public ByteBuffer pointerToJavaBuffer(long ptrPart1, long ptrPart2, long size)
	{
		return new PartNativeMemoryNative().getBuffer(ptrPart1, ptrPart2, size).order(ByteOrder.nativeOrder());
	}
	static public long javaBufferToPointer(ByteBuffer bb, int ptrPartIndex)
	{
		return new PartNativeMemoryNative().getNativePointer(bb, ptrPartIndex);
	}
}
