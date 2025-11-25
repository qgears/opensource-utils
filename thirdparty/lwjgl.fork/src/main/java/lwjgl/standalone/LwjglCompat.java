package lwjgl.standalone;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

/**
 * Compatibility layer - provide same API and wrap differences between lwjg 2 and 3.
 * 
 * See the same class lwjgl3.int project
 */
public class LwjglCompat {
	public static void glVertexPointer(int size, int stride, FloatBuffer m) {
		GL11.glVertexPointer(size, stride,m);
	}

	public static void glLoadMatrix(FloatBuffer m) {
		GL11.glLoadMatrix(m);
	}
}
