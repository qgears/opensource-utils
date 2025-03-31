package hu.qgears.opengl.commons;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;

/**
 * Interface to the Window and OpenGL context.
 * There are multiple implementations of this interface.
 * All methods must be called from the same thread.
 * @author rizsi
 *
 */
public interface IGlContextProvider {

	/**
	 * This method is called before any other.
	 * This method may be called more than once, 
	 * implementation must handle multi calls.
	 */
	void loadNatives();

	/**
	 * The size of the client area of the openGL window.
	 * @return
	 */
	SizeInt getClientAreaSize();

	/**
	 * This method is called after loadNAtives() but before anything else.
	 */
	void init();

	/**
	 * This method is called to create a window on the screen
	 * and a OpenGL context onto the screen.
	 * @param initFullscreen
	 * @param initTitle
	 * @param size
	 * @throws Exception
	 */
	void openWindow(boolean initFullscreen, String initTitle, SizeInt size) throws Exception;

	/**
	 * Has the user requested to close the application?
	 * This method is queried by the application regularly.
	 * @return true means that the application should close itself.
	 */
	boolean isCloseRequested();

	/**
	 * Is the application visible on the screen?
	 * @return false means that we don't need to refresh the
	 *  content.
	 */
	boolean isVisible();

	/**
	 * Query whether the application graphics is dirty or not 
	 * (eg. window resize makes it dirty)  
	 * @return true means that the window has to be redrawn.
	 */
	boolean isDirty();
	/**
	 * Process all messages received from the OS.
	 * Calling this method is part of the main loop.
	 */
	void processMessages();
	/**
	 * Update the screen with the frame currently drawn.
	 * Calls glXSwapBuffers and similar platform methods.
	 * Also calls processMessages.
	 */
	void update();

	void dispose();

	IKeyboard getKeyboard();
	/**
	 * Make the application to run in fullscreen.
	 * @param fullscreen
	 * @throws Exception
	 */
	void setFullScreen(boolean fullscreen) throws Exception;
	
	boolean isFullScreen();

	IMouse getMouse();

	void setVSyncEnabled(boolean vSyncEnabled);

}
