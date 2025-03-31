package hu.qgears.opengl.commons.example;

import hu.qgears.opengl.glut.Glut;
import hu.qgears.opengl.glut.GlutInstance;

public class GlutExample01 {
	public static void main(String[] args) {
		new GlutExample01().run();
	}

	private void run() {
		GlutInstance.getInstance();
		Glut glut=new Glut();
		glut.init();
		glut.openWindow(false, 1024, 768, "Example 1");
		doBunchOfLoop(glut);
		glut.setFullScreen(true, 1024, 768);
		int w=glut.getScreenWidth();
		int h=glut.getScreenHeight();
		doBunchOfLoop(glut);
		glut.setFullScreen(false, 1024, 768);
		doBunchOfLoop(glut);
		/*For debug purposes*/
		System.out.println("Fullscreen width and height: ["+w+", "+h+"]");//NOSONAR
	}

	private void doBunchOfLoop(Glut glut) {
		int ctr=0;
		while(ctr<100)
		{
			glut.mainLoopEvent();
			glut.testDrawBasicScene();
			glut.swapBuffers();
			ctr++;
		}
	}
}
