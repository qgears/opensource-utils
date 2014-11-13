package hu.qgears.opengl.commons.container;

import java.util.List;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.IMouse;

public interface IOGLContainer {

	void setWindowSize(SizeInt sizeInt);

	void setInitTitle(String string);

//	void initialize();

	SizeInt getClientAreaSize();

	SizeInt getSize();

	void setFullScreen(boolean b) throws Exception;

	/**
	 * Get the mouse object that can be accessed by this application.
	 * @return
	 */
	IMouse getMouseObject();

	/**
	 * Set the application and launch the client
	 * @param app
	 */
	void setThisApplication(IOGlApplication app);

	List<IOGLContainer> getApplications();
	void setActiveApplication(IOGLContainer application);
	/**
	 * Switch to the next application.
	 * @return the application selected after the switch
	 */
	IOGLContainer nextApplication();
	/**
	 * Switch to this application.
	 * @return
	 */
	IOGLContainer thisApplication();

	void setPreviousImage(NativeImage im);
	NativeImage getPreviousImage();

	/**
	 * Exit the whole application (process).
	 * Close other 'tabs' too.
	 */
	void exit();

	/**
	 * Close this tab only.
	 */
	void dispose();

	void setVSyncEnabled(boolean curr);

	IOGLContainer getActiveApplication();
	
	/**
	 * Is this application currently active in the OpenGL frame? 
	 * @return
	 */
	boolean isActive();

	/**
	 * Is the OpenGL frame fullscreen?
	 * @return
	 */
	boolean isFullscreen();

	IOGlApplication getThisApplication();
}
