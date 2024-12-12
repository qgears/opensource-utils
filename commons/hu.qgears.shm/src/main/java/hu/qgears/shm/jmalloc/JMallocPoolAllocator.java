package hu.qgears.shm.jmalloc;

/**
 * The pool allocator will be called to allocate a new pool when the existing pools can not serve
 * a new allocation.
 */
public interface JMallocPoolAllocator {
	/**
	 * The allocator either has to allocate a valid pool with given size or throw OOM.
	 * @param poolSize
	 * @return
	 */
	JMallocPool allocateNewPool(int poolSize) throws OutOfMemoryError;
}
