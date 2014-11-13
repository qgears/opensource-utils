package hu.qgears.commons.mem;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;

import java.nio.ByteBuffer;


/**
 * The default Java implementation of native memory buffers.
 * Does not support native pointer.
 * @author rizsi
 *
 */
public class DefaultJavaNativeMemory extends AbstractReferenceCountedDisposeable implements INativeMemory {
	ByteBuffer ptr;
	public DefaultJavaNativeMemory(long size) {
		if(((int)size)!=size)
		{
			throw new NativeMemoryException("Required size is too big "+size);
		}
		ptr=ByteBuffer.allocateDirect((int)size);
	}
	public DefaultJavaNativeMemory(ByteBuffer ptr) {
		this.ptr=ptr;
	}
	@Override
	protected void singleDispose() {
		// Nothing to do ByteBuffers allocated using Java's default mechanism
		// finalize itself when garbage collected
	}
	@Override
	public ByteBuffer getJavaAccessor() {
		return ptr;
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
		return ptr.capacity();
	}
}
