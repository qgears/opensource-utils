package hu.qgears.commons.mem;

/**
 * Memory allocator using ByteBuffer.allocateDirect()
 * @author rizsi
 *
 */
public class DefaultJavaNativeMemoryAllocator implements INativeMemoryAllocator {
	private static final int align=1;
	private static final DefaultJavaNativeMemoryAllocator instance =
			new DefaultJavaNativeMemoryAllocator();
	
	private DefaultJavaNativeMemoryAllocator() {
		//prevent direct instantiation - use getInstance
		// In case there is a dispose issue that will surface earlier.
		allocateNativeMemory(8).dispose();
	}
	
	@Override
	public INativeMemory allocateNativeMemory(long size) {
		return allocateNativeMemory(size, align);
	}

	public static DefaultJavaNativeMemoryAllocator getInstance() {
		return instance;
	}

	/**
	 * Maximum alignment is 16.
	 */
	@Override
	public INativeMemory allocateNativeMemory(long size, int align) {
		if(align>16)
		{
			throw new NativeMemoryException("Alignment > 16 is not supported");
		}
		return new DefaultJavaNativeMemory(size);
	}

	@Override
	public int getDefaultAlignment() {
		return align;
	}

}
