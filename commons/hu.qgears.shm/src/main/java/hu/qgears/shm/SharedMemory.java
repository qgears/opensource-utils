package hu.qgears.shm;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.commons.mem.INativeMemory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A shared memory. The shared memory can be accessed from several processes
 *  (even non-java processes).
 *  Implementation is platform specific!
 *  
 *  Not thread safe!
 * 
 * After creation its size can not be changed. 
 * @author rizsi
 *
 */
public class SharedMemory extends AbstractReferenceCountedDisposeable implements INativeMemory {
	private SharedMemoryNative nat;
	private long size;
	private ByteBuffer javaAccessor;
	private long nativePointer1;
	private long nativePointer2;
	/**
	 * See UtilSharedMemory.createSharedMemory()
	 * @param id
	 * @param createType
	 * @param size
	 */
	protected SharedMemory(String id, ECreateType createType, long size) {
		nat=new SharedMemoryNative();
		nat.init(id, createType.ordinal(), size);
		this.size=nat.getSize();
		this.javaAccessor=nat.getAccessor();
		this.javaAccessor.order(ByteOrder.nativeOrder());
		this.nativePointer1=nat.getNativePointer1();
		this.nativePointer2=nat.getNativePointer2();
	}
	protected SharedMemory(long id) {
		nat=new SharedMemoryNative();
		nat.init2(id);
		this.size=nat.getSize();
		this.javaAccessor=nat.getAccessor();
		this.javaAccessor.order(ByteOrder.nativeOrder());
		this.nativePointer1=nat.getNativePointer1();
		this.nativePointer2=nat.getNativePointer2();
	}
	protected SharedMemory(File f) {
		nat=new SharedMemoryNative();
		nat.initFile(f.getAbsolutePath());
		this.size=nat.getSize();
		this.javaAccessor=nat.getAccessor();
		this.javaAccessor.order(ByteOrder.nativeOrder());
		this.nativePointer1=nat.getNativePointer1();
		this.nativePointer2=nat.getNativePointer2();
	}
	@Override
	protected void singleDispose() {
		nat.nativeDispose(false);
	}
	/**
	 * Get the size of this shared memory obejct.
	 * On Windows we can not reliably query the size of created
	 * shared memory.
	 * 
	 * This value must not be used to address objects.
	 * @return
	 */
	public long getSize()
	{
		checkDisposed();
		return size;
	}
	/**
	 * Synchronize the contents of this shared memory object to the RAM (from CPU cache).
	 * On Linux call: msync(str->ptr, str->size, MS_INVALIDATE);
	 * @param write whether to write to disk or read from disk
	 */
	public void sync(boolean write)
	{
		nat.sync(write);
	}
	@Override
	public ByteBuffer getJavaAccessor() {
		checkDisposed();
		return javaAccessor;
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
	/**
	 * Delete the shared memory associated with this object. (On Linux unlink /dev/shm/myshmfile)
	 * Automatically calls dispose and this object is unusable after calling this method.
	 */
	public synchronized void deleteSharedMemory()
	{
		nat.nativeDispose(true);
		markDisposed();
	}
}
