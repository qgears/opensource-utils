package hu.qgears.opengl.commons.container;

public interface IOGlApplication {

	void initialize() throws Exception;

	void logic();

	void afterBufferSwap();

	void beforeBufferSwap();

	void render();

	void keyDown(int eventKey, char ch, boolean shift, boolean ctrl,
			boolean alt, boolean special) throws Exception;

	boolean isDirty();

}
