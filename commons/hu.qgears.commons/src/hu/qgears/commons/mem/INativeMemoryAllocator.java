package hu.qgears.commons.mem;

public interface INativeMemoryAllocator {
	/**
	 * Allocate native memory.
	 * 
	 * The returned native memory:
	 *  * has the required size
	 *  * order(ByteOrder.native);
	 * 
	 * @param size size of the memory to be allocated.
	 * @param align memory align. 0 means default. Implementation may not support (throw runtime exception in case of different from 0).
	 * @return handle to native memory
	 */
	INativeMemory allocateNativeMemory(long size, int align);
	/**
	 * Allocate native memory.
	 * 
	 * The returned native memory:
	 *  * has the required size
	 *  * order(ByteOrder.native);
	 * 
	 * @param size size of the memory to be allocated.
	 * align to the default of this allocator memory align. 0 means default. Implementation may not support (throw runtime exception in case of different from 0).
	 * @return handle to native memory
	 */
	INativeMemory allocateNativeMemory(long size);
	/**
	 * Get the default alignment of this allocator.
	 * @return
	 */
	int getDefaultAlignment();
}
