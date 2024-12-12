package hu.qgears.shm;

import hu.qgears.commons.mem.NativeMemoryException;

import java.nio.ByteBuffer;

/**
 * Native methods of the shared memory implementation. 
 * @author rizsi
 *
 */
class SharedMemoryNative {
	/**
	 * Native struct ptr.
	 */
	private long ptr;
	/**
	 * Create a shared memory object by id.
	 * @param id
	 * @throws NativeMemoryException
	 */
	protected native void init2(long id) throws NativeMemoryException;
	/**
	 * Create a shared memory object by shm name.
	 * @param id
	 * @param createType
	 * @param size
	 * @throws NativeMemoryException
	 */
	protected native void init(String id, int createType, long size) throws NativeMemoryException;
	/**
	 * Map a file into a byte buffer.
	 * @param fileName
	 */
	protected native void initFile(String fileName);
	protected native void sync(boolean write);
	/**
	 * Delete all resources allocated by this object.
	 * @param deleteShm true means that the shared memory object must be deleted.
	 */
	protected native void nativeDispose(boolean deleteShm);
	/**
	 * Get the size of the native memory object.
	 * @return
	 */
	protected native long getSize();
	/**
	 * Get accessor to the native memory object.
	 * @return
	 */
	protected native ByteBuffer getAccessor();
	protected native long getNativePointer1();
	protected native long getNativePointer2();
	native protected void deleteSharedMemoryById(long id);
}
