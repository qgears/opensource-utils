package hu.qgears.shm.part;

import java.nio.ByteBuffer;

/**
 * Manually grab a part of an already allocated memory.
 * Can be used to:
 *  * manually partition allocated big chunk of memory
 *  * implement allocator in Javas
 *
 */
class PartNativeMemoryNative
{
	protected native ByteBuffer getByteBuffer(ByteBuffer host, long offset, long size);
	protected native long getNativePointer(ByteBuffer javaAccessor, int i);
	protected native ByteBuffer getBuffer(long ptr1, long ptr2, long size);
	/** See {@link PartNativeMemory}.getOffset() */
	protected static native long getOffset(ByteBuffer base, ByteBuffer relative);
	/** See {@link PartNativeMemory}.clearBuffer() */
	protected static native void clearBuffer(ByteBuffer buffer);
}
