package org.lwjgl.system;

import java.nio.ByteBuffer;
/**
 * Interface from LWJGL 3. Does not used in LWJGL 2, but part of compatibility layer.
 */
public interface FunctionProvider {
	public long getFunctionAddress(ByteBuffer functionName);
}
