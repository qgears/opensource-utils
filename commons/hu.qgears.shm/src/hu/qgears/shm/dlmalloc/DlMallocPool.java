package hu.qgears.shm.dlmalloc;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.UtilSharedMemory;

import java.nio.ByteBuffer;


/**
 * Use dlmalloc -  Doug Lea - ftp://gee.cs.oswego.edu/pub/misc/malloc.c
 * on a native memory to allocate parts of it as single native memory chunks.
 * 
 * @author rizsi
 */
public class DlMallocPool extends AbstractReferenceCountedDisposeable implements INativeMemoryAllocator {
	// The buffer that hosts this native memory allocator.
	private INativeMemory host;
	DlMallocPoolNative nat;
	volatile private int allocatedCounter=0;
	public static final int defaultAlignment=1;
	/**
	 * Create a dlmalloc
	 * @param host
	 * @param synchronize use locks inside dlmalloc implementation - makes the obejct thread safe
	 * @throws NativeLoadException 
	 */
	public DlMallocPool(INativeMemory host, boolean synchronize) throws NativeLoadException
	{
		UtilSharedMemory.getInstance();
		this.host=host;
		nat=new DlMallocPoolNative();
		nat.init(host.getJavaAccessor(), host.getSize(), synchronize);
	}
	@Override
	public synchronized DlMallocMemory allocateNativeMemory(long size, int align) {
		DlMallocMemory ret=new DlMallocMemory(this, size, align);
		allocatedCounter++;
		return ret;
	}
	public synchronized DlMallocMemory allocateNativeMemory(long size) {
		return allocateNativeMemory(size, defaultAlignment);
	}
	@Override
	protected void singleDispose() {
		// Nothing to do. dlmalloc pool is self contained.
		// But native allocated struct must be disposed.
		nat.nativeDispose();
	}
	public INativeMemory getHost() {
		return host;
	}
	public synchronized long getCurrentAllocated()
	{
		return nat.getAllocatedSize();
	}
	public synchronized long getMaxAllocated()
	{
		return nat.getMaxAllocated();
	}
	public synchronized void free(DlMallocMemory dlMallocMemory, ByteBuffer bb) {
		nat.dlfree(bb);
		allocatedCounter--;
	}
	public int getAllocatedCounter() {
		return allocatedCounter;
	}
	@Override
	public int getDefaultAlignment() {
		return defaultAlignment;
	}
}
