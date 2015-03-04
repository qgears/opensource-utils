package hu.qgears.shm.dlmalloc;

import java.nio.ByteBuffer;

/**
 * Class that backs native implementation.
 * @author rizsi
 *
 */
class DlMallocPoolNative {
	/**
	 * Native struct pointer.
	 */
	private long ptr;
	protected native void init(ByteBuffer javaAccessor, long size, boolean synchronize);
	protected native ByteBuffer dlalloc(long size, int align);
	protected native void dlfree(ByteBuffer bb);
	protected native long getRelativeAddress(ByteBuffer bb);
	protected native long getNativePointer(ByteBuffer bb, int i);
	protected native void nativeDispose();
	protected native long getAllocatedSize();
	protected native long getMaxAllocated();
}
