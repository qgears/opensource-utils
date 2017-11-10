package hu.qgears.commons.mem;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;


/**
 * The default Java implementation of native memory buffers.
 * Does not support native pointer.
 * @author rizsi
 *
 */
public class DefaultJavaNativeMemory extends AbstractReferenceCountedDisposeable implements INativeMemory {
	private ByteBuffer ptr;
	
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
		try {
			final Method cleanerMethod = ptr.getClass().getMethod("cleaner");
			cleanerMethod.setAccessible(true);
			final Object cleaner = cleanerMethod.invoke(ptr);
			final Method cleanMethod = cleaner.getClass().getMethod("clean");
			cleanMethod.setAccessible(true);
			cleanMethod.invoke(cleaner);
		} catch (final Exception e) {
			throw new NativeMemoryException("Exception during disposal", e);
		}
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
