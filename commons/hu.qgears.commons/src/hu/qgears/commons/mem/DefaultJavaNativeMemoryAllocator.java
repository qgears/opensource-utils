package hu.qgears.commons.mem;

/**
 * Memory allocator using ByteBuffer.allocateDirect()
 * @author rizsi
 *
 */
public class DefaultJavaNativeMemoryAllocator implements INativeMemoryAllocator {
	int align=1;
	@Override
	public INativeMemory allocateNativeMemory(long size) {
		return allocateNativeMemory(size, align);
	}

	public static DefaultJavaNativeMemoryAllocator getInstance() {
		return new DefaultJavaNativeMemoryAllocator();
	}

	@Override
	public INativeMemory allocateNativeMemory(long size, int align) {
		if(align>16)
		{
			// TODO check alignment features of Java allocator!
			throw new NativeMemoryException("Alignment > 16 is not supported");
		}
		return new DefaultJavaNativeMemory(size);
	}

	@Override
	public int getDefaultAlignment() {
		return align;
	}

}
