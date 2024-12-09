package hu.qgears.commons.mem;

/**
 * Native memory with a relative address to its owner host.
 */
public interface INativeMemoryWithRelativeAddress extends INativeMemory {
	/**
	 * Return the relative address to the host.
	 * @return
	 */
	long getRelativeAddress();
	/**
	 * Get the host of the native memory chunk.
	 * relative address is relative to the beginning of the host chunk
	 * and the whole chunk fits the host memory.
	 */
	INativeMemory getHost();
	/**
	 * Get the relative address of the next byte in the host memory.
	 * @return getRelativeAddress()+getSize()
	 */
	long getAfterRelativeAddress();
}
