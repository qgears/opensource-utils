package hu.qgears.commons.mem;

import java.nio.ByteBuffer;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;

/**
 * Implementation of the {@link INativeMemory} interface when the 
 * embedded {@link ByteBuffer} object is backd by a heap based array
 * so it is not direct memory and thus need not be disposed.
 * Can be used with any API that requires an {@link INativeMemory} object but does not depend on that to be
 * direct memory.
 */
public class HeapJavaMemory extends AbstractReferenceCountedDisposeable implements INativeMemory {
	private ByteBuffer ptr;
	public HeapJavaMemory(ByteBuffer ptr) {
		super();
		this.ptr = ptr;
	}
	@Override
	public ByteBuffer getJavaAccessor() {
		return ptr;
	}
	/**
	 * Dummy implementation because native pointer is not accessible.
	 */
	@Override
	public long getNativePointer1() {
		return 0;
	}
	/**
	 * Dummy implementation because native pointer is not accessible.
	 */
	@Override
	public long getNativePointer2() {
		return 0;
	}
	@Override
	public long getSize() {
		return ptr.capacity();
	}
	@Override
	protected void singleDispose() {
		// There is nothing to do here because the object is Java heap based and garbage collected.
	}
}
