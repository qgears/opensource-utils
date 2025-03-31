package hu.qgears.opengl.commons.example;

import hu.qgears.opengl.glut.Glut;
import hu.qgears.opengl.glut.GlutInstance;

public class GlutExample00 {
	public static void main(String[] args) {
		new GlutExample00().run();
	}

	private void run() {
		GlutInstance.getInstance();
		Glut glut=new Glut();
		glut.nativeTest();
	}
}
