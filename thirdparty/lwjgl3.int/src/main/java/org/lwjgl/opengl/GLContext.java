package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;

/**
 * Try to implement the functionality of {@link GLContext} API from LWJGL 2.x
 * over 3.x. Helps to make existing codes compile, but sadly not fully compatible.
 */
public class GLContext {

	/**
	 * Create and bind GL implementation to LWJGL. XXX might be not fully compatible
	 * with 2.x version on GLContext
	 * 
	 * @param ctx
	 */
	public static void useContext(Object ctx) throws LWJGLException {
		if (ctx != null) {
			GL.create();
			GL.createCapabilities();
		} else {
			GL.destroy();
		}
	}

	public static GLCapabilities getCapabilities() {
		return GL.getCapabilities();
	}

}
