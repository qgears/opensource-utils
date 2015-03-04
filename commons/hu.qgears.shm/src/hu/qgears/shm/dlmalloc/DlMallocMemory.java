package hu.qgears.shm.dlmalloc;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryWithRelativeAddress;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Memory allocated from a dlmalloc pool.
 * @author rizsi
 *
 */
public class DlMallocMemory extends AbstractReferenceCountedDisposeable implements INativeMemoryWithRelativeAddress {
	private DlMallocPool host;
	private ByteBuffer bb;
	private long nativePointer1, nativePointer2;
	private long relativeAddress;
	public DlMallocMemory(DlMallocPool dlMallocPool, long size, int align) {
		this.host=dlMallocPool;
		bb=host.nat.dlalloc(size, align);
		this.bb.order(ByteOrder.nativeOrder());
		nativePointer1=host.nat.getNativePointer(bb, 1);
		nativePointer2=host.nat.getNativePointer(bb, 2);
		relativeAddress=host.nat.getRelativeAddress(bb);
	}

	@Override
	protected void singleDispose() {
		if(host.isDisposed())
		{
			return;
		}
		host.free(this, bb);
	}

	@Override
	public ByteBuffer getJavaAccessor() {
		checkDisposed();
		return bb;
	}

	@Override
	public long getNativePointer1() {
		checkDisposed();
		return nativePointer1;
	}

	@Override
	public long getNativePointer2() {
		checkDisposed();
		return nativePointer2;
	}

	@Override
	public long getRelativeAddress() {
		checkDisposed();
		return relativeAddress;
	}

	@Override
	public INativeMemory getHost() {
		checkDisposed();
		return host.getHost();
	}

	@Override
	public long getSize() {
		checkDisposed();
		return bb.capacity();
	}

	@Override
	public long getAfterRelativeAddress() {
		checkDisposed();
		return getRelativeAddress()+getSize();
	}

}
