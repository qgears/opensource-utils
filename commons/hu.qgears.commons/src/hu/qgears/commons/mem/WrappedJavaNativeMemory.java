package hu.qgears.commons.mem;

import java.nio.ByteBuffer;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;

public class WrappedJavaNativeMemory extends AbstractReferenceCountedDisposeable implements INativeMemory
{
	INativeMemory parent;
	ByteBuffer javaAccessor;
	/**
	 * Wrap a parent native memory.
	 * Increment reference count on parent
	 * when constructed
	 * and decrement on disposal.
	 * 
	 * position, limit and mark of the hosting byte buffer will not be modified.
	 * 
	 * @param parent
	 */
	public WrappedJavaNativeMemory(INativeMemory parent, int position, int limit) {
		super();
		ByteBuffer bb=parent.getJavaAccessor().duplicate();
		bb.position(position);
		bb.limit(limit);
		javaAccessor=bb.slice();
		this.parent = parent;
		parent.incrementReferenceCounter();
	}

	@Override
	public ByteBuffer getJavaAccessor() {
		return javaAccessor;
	}

	@Override
	public long getNativePointer1() {
		throw new NativeMemoryException("Feature not supported");
	}

	@Override
	public long getNativePointer2() {
		throw new NativeMemoryException("Feature not supported");
	}

	@Override
	public long getSize() {
		return javaAccessor.capacity();
	}

	@Override
	protected void singleDispose() {
		parent.decrementReferenceCounter();
	}

}
