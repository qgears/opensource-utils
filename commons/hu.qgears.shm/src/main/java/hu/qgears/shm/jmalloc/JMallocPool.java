package hu.qgears.shm.jmalloc;

import java.nio.ByteBuffer;

import hu.qgears.commons.mem.INativeMemory;

/**
 * A memory area with continuous address.
 */
public class JMallocPool {
	final protected INativeMemory nmem;
	/** Handled by {@link JMalloc} */
	protected int poolOffset;
	public JMallocPool(INativeMemory nmem) {
		super();
		this.nmem = nmem;
	}
	public ByteBuffer getJavaAccessor() {
		return nmem.getJavaAccessor();
	}
	public INativeMemory getNativeMemory() {
		return nmem;
	}
}
