package lwjgl.standalone;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

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
	
	public static void glMaterial( int face, int pname, FloatBuffer params) {
		GL11.glMaterial(face,pname,params);
	}

	public static void glMultMatrix(FloatBuffer fb) {
		GL11.glMultMatrix(fb);
	}

	public static void glGetInteger(int glMaxTextureSize, IntBuffer tempIntBuffer) {
		GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE, tempIntBuffer);
	}

	public static ContextCapabilities getCapabilities() {
		return GLContext.getCapabilities();
	}

}
