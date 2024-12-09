package hu.qgears.commons.mem;

import hu.qgears.commons.IReferenceCountedDisposeable;

import java.nio.ByteBuffer;


/**
 * Wrapper for java ByteBuffer that supports:
 *  * explicite freeing
 *  * reference counter
 * @author rizsi
 *
 */
public interface INativeMemory extends IReferenceCountedDisposeable {
	/**
	 * Get a byte buffer that points to this memory area.
	 * The byte order of the byte buffer is set to native
	 * before returning!
	 * (.order(ByteOrder.nativeOrder()))
	 * @return
	 */
	ByteBuffer getJavaAccessor();
	/**
	 * Translate the native pointer to a Java long.
	 * Only use in platform dependent code! Implementation may not support this method!
	 * @return the low 64 bits of the pointer
	 */
	long getNativePointer1();
	/**
	 * Translate the native pointer to a Java long.
	 * Only use in platform dependent code! Implementation may not support this method!
	 * @return the high 64 bits of the pointer (0 on current implementations)
	 */
	long getNativePointer2();
	/**
	 * Get the size of the native memory block.
	 * @return
	 */
	long getSize();
}
