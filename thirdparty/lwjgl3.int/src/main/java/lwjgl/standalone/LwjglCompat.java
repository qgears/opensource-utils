package lwjgl.standalone;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
/**
 * Compatibility layer - provide same API and wrap differences between lwjg 2 and 3.
 * 
 * See the same class lwjgl.fork
 */
public class LwjglCompat {
	public static void glVertexPointer(int size, int stride, FloatBuffer m) {
//		GL11.glVertexPointer(size, stride,m);
		//LWJGL3
		GL11.glVertexPointer(size, GL11.GL_FLOAT, stride, m);
	}

	public static void glLoadMatrix(FloatBuffer m) {
//		GL11.glLoadMatrix(m);
		//LWJGL3
		GL11.glLoadMatrixf(m);
	}
}
