package hu.qgears.opengl.commons.example;

import hu.qgears.opengl.commons.Camera;
import hu.qgears.opengl.commons.TargetRectangle;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.context.RGlContext;
import hu.qgears.opengl.glut.Glut;
import hu.qgears.opengl.glut.GlutInstance;
import lwjgl.standalone.BaseAccessor;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class GlutExample02 {
	private static Camera camera=new Camera();
	private static final int WIDTH=1024;
	private static final int HEIGHT=768;
	public static void main(String[] args) throws LWJGLException {
		GlutInstance.getInstance();
		BaseAccessor.initLwjglNatives();
		Glut glut=new Glut();
		glut.init();
		glut.openWindow(false, WIDTH, HEIGHT, "example 2");
		GLContext.useContext(glut);
		doBunchOfLoop(glut);
		glut.setFullScreen(true, WIDTH, HEIGHT);
		doBunchOfLoop(glut);
		glut.setFullScreen(false, WIDTH, HEIGHT);
		doBunchOfLoop(glut);
	}

	private static void doBunchOfLoop(Glut glut) {
		int ctr=0;
		while(ctr<200)
		{
			glut.mainLoopEvent();
			drawScene();
			glut.swapBuffers();
			ctr++;
		}
	}

	private static void drawScene() {
		// A kép rajzolásának elkezdését jelezzük ezzel a paranccsal
		GL11.glRenderMode(GL11.GL_RENDER);
		// Beállítjuk a kamera pozícióját.
		// Innentől mindent úgy rajzol az OpenGL, hogy a megadott
		// Pozícióba képzeli a kamerát
		UtilGl.init3D(WIDTH, HEIGHT, 30.0f, 1.0f);
		camera.setCamera();
		// háttér törlése
		GL11.glClearColor(0,0,1,1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		// fehér háromszög rajzolása
		renderVideo();
	}
	private static void renderVideo() {
		RGlContext glContext=new RGlContext();
		// háttér törlése
		GL11.glClearColor(0,1,0,.3f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		float sizeX=1024;
		float sizeY=768;
		float scale=.2f;
		UtilGl.setColor(new Vector3f(1,0,0));
		UtilGl.translate(GL11.GL_MODELVIEW, new Vector3f(0,0,-10));
		TargetRectangle rect=new TargetRectangle(
				(Vector2f)new Vector2f(-sizeX/2,sizeY/2).scale(scale),
				(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
		);
		UtilGl.drawRectangle(glContext, rect, new Vector4f(1,0,0,.5f));
		rect=new TargetRectangle(
				(Vector2f)new Vector2f(-sizeX/2+sizeX/2,sizeY/2).scale(scale),
				(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
		);
		UtilGl.drawRectangle(glContext, rect, new Vector4f(1,0,0,1f));
	}
}
