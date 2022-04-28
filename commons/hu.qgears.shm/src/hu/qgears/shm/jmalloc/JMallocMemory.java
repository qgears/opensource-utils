package hu.qgears.shm.jmalloc;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryWithRelativeAddress;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Memory allocated from a {@link JMalloc} pool.
 * Reference counted and must be disposed in order to reclaim memory.
 * The class has a finalizer that will dispose the object in case it was not disposed by the code.
 */
public class JMallocMemory extends AbstractReferenceCountedDisposeable implements INativeMemoryWithRelativeAddress, AutoCloseable {
	private JMalloc host;
	private ByteBuffer bb;
	private int relativeAddress;
	protected int start, end;
	protected JMallocPool pool;
	public JMallocMemory(JMalloc jMallocPool, JMallocPool pool, ByteBuffer bb,
			int size, int align, int relativeAddress, int start, int end) {
		this.pool=pool;
		this.host=jMallocPool;
		this.bb=bb;
		this.bb.order(ByteOrder.nativeOrder());
		this.relativeAddress=relativeAddress;
		this.start=start;
		this.end=end;
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
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public long getNativePointer2() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public long getRelativeAddress() {
		checkDisposed();
		return relativeAddress;
	}

	@Override
	public INativeMemory getHost() {
		checkDisposed();
		return pool.getNativeMemory();
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
	@Override
	protected void finalize() throws Throwable {
		dispose();
	}
	/**
	 * Decrement reference counter.
	 * May not dispose the object if 0 is not reached.
	 */
	@Override
	public void close() {
		decrementReferenceCounter();
	}
}
