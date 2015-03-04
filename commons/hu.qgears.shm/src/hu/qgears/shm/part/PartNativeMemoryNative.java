package hu.qgears.shm.part;

import java.nio.ByteBuffer;

/**
 * Manually grab a part of an already allocated memory.
 * Can be used to:
 *  * manually patition allocated big chunk of memory
 *  * implement allocator in Javas
 * @author rizsi
 *
 */
class PartNativeMemoryNative
{
	protected native ByteBuffer getByteBuffer(ByteBuffer host, long offset, long size);
	protected native long getNativePointer(ByteBuffer javaAccessor, int i);
	protected native ByteBuffer getBuffer(long ptr1, long ptr2, long size);
}
