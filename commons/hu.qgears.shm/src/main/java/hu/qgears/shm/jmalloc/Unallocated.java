package hu.qgears.shm.jmalloc;

/**
 * Represents unallocated pieces of memory available for allocation in one of the
 * already allocated pools.
 */
public class Unallocated {
	/**
	 * Memory pool containing this unallocated memory area.
	 */
	public final JMallocPool pool;
	/**
	 * Size of the unallocated area in bytes.
	 */
	public final int size;
	/**
	 * Including.
	 */
	public final int start;
	/**
	 * Excluding.
	 */
	public final int end;
	public Unallocated(JMallocPool pool, int size, int start, int end) {
		super();
		this.pool=pool;
		this.size = size;
		this.start = start;
		this.end = end;
	}
}
