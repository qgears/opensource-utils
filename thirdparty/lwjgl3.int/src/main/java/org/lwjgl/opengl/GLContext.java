package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.system.FunctionProvider;

/**
 * Try to implement the functionality of {@link GLContext} API from LWJGL 2.x
 * over 3.x. Helps to make existing codes compile, but sadly not fully compatible.
 */
public class GLContext {

	private static boolean created = false;
	
	/**
	 * Create and bind GL implementation to LWJGL. XXX might be not fully compatible
	 * with 2.x version on GLContext
	 * 
	 * @param ctx
	 */
	public static void useContext(Object ctx) throws LWJGLException {
		if (ctx != null) {
			if (!created) {
				created = true;
				if (ctx instanceof FunctionProvider) {
					GL.create((FunctionProvider) ctx);
				} else {
					GL.create();
				}
			}
			GL.createCapabilities();
		} else {
			if (created) {
				GL.destroy();
				created = false;
			}
		}
	}

	public static GLCapabilities getCapabilities() {
		return GL.getCapabilities();
	}

}
