package lwjgl.standalone;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
/**
 * Compatibility layer - provide same API and wrap differences between lwjg 2 and 3.
 * 
 * See the same class lwjgl.fork
 */
public class LwjglCompat {
	public static void glVertexPointer(int size, int stride, FloatBuffer m) {
		//LWJGL3
		GL11.glVertexPointer(size, GL11.GL_FLOAT, stride, m);
	}

	public static void glLoadMatrix(FloatBuffer m) {
		//LWJGL3
		GL11.glLoadMatrixf(m);
	}
	
	public static void glMaterial( int face, int pname, FloatBuffer params) {
		GL11.glMaterialfv(face,pname,params);
	}
	
	public static void glMultMatrix(FloatBuffer fb) {
		GL11.glMultMatrixf(fb);
	}

	public static void glGetInteger(int glMaxTextureSize, IntBuffer tempIntBuffer) {
		GL11.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, tempIntBuffer);
	}

	public static GLCapabilities getCapabilities() {
		return GL.getCapabilities();
	}
}
