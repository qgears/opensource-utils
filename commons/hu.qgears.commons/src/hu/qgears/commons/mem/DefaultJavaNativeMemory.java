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
		/*
		 * Disallowing parallel access to the 'cleaner' and 'clean' methods as 
		 * long as their accessibility is set to 'true'.
		 */
		@SuppressWarnings("squid:S3011")
		Method cleanerMethod = null;
		@SuppressWarnings("squid:S3011")
		Method cleanMethod = null;
		try {
			cleanerMethod = ptr.getClass().getMethod("cleaner");
			synchronized (cleanerMethod) {
				try {
					cleanerMethod.setAccessible(true);
	
					final Object cleanerInstance = cleanerMethod.invoke(ptr);
	
					cleanMethod = cleanerInstance.getClass().getMethod("clean");
	
					synchronized (cleanMethod) {
						try {
							cleanMethod.setAccessible(true);
							cleanMethod.invoke(cleanerInstance);
						} finally {
							cleanMethod.setAccessible(false);
						}
					}
				} finally {
					cleanerMethod.setAccessible(false);
				}
			}
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
