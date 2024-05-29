package hu.qgears.commons.mem;

import java.nio.ByteBuffer;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.BufferDisposal.Cleaner;

/**
 * The default Java implementation of native memory buffers with optional
 * {@link BufferDisposal#programmaticDispose programmatic memory disposal} and
 * {@link AbstractReferenceCountedDisposeable reference counting}. 
 * 
 * Does not support native pointer.
 * 
 * @see #programmaticDispose {@link BufferDisposal#programmaticDispose}  for memory disposal
 * policy 
 */
public class DefaultJavaNativeMemory 
extends AbstractReferenceCountedDisposeable implements INativeMemory {
	
	private Cleaner cleaner = BufferDisposal.getCleanerInstance();
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
	
	/*
	 * Disallowing parallel access to the 'cleaner' and 'clean' methods as 
	 * long as their accessibility is set to 'true'.
	 */
	@SuppressWarnings("squid:S3011")
	@Override
	protected void singleDispose() {
		try {
			cleaner.clean(ptr);
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
