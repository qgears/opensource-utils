package hu.qgears.opengl.lwjgl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.input.IKeyboard;
import lwjgl.standalone.BaseAccessor;

public class GLContextProviderLwjgl implements IGlContextProvider{
	private static final Logger LOG = LogManager.getLogger(
			GLContextProviderLwjgl.class);
	

	private boolean fullscreen;

	private DisplayMode mode;


	private SizeInt fullscreenSize;


	private DisplayMode windowMode;

	private MouseImplLwjgl mouse;

	@Override
	public void loadNatives() {
		BaseAccessor.initLwjglNatives();
	}

	private SizeInt getFullscreenSize()
	{
		int defaultWidth=Display.getDesktopDisplayMode().getWidth();
		int defaultHeight=Display.getDesktopDisplayMode().getHeight();
		return new SizeInt(defaultWidth, defaultHeight);
	}
	@Override
	public SizeInt getClientAreaSize() {
		if(fullscreen)
		{
			return getFullscreenSize();
		}else
		{
			return new SizeInt(windowMode.getWidth(), windowMode.getHeight());
		}
	}

	@Override
	public void init() {
		
	}
	@Override
	public void openWindow(boolean initFullscreen, String initTitle,
			SizeInt size) throws LWJGLException {
		if(fullscreenSize==null)
		{
			fullscreenSize=getFullscreenSize();
		}
		windowMode=new DisplayMode(size.getWidth(), size.getHeight());
		if(initFullscreen)
		{
			switchToFullScreen();
		}else
		{
			switchToWindowMode();
		}
		Display.setTitle(initTitle);
		PixelFormat pf=new PixelFormat(8, 0, 0);
		Display.create(pf);
		Display.setVSyncEnabled(true);
		fullscreen=initFullscreen;
	}
	/**
	 * Ablakos módba váltás.
	 * @throws LWJGLException
	 */
	protected void switchToWindowMode() throws LWJGLException {
		mode = getWindowMode();
		if(LOG.isDebugEnabled()){
			LOG.debug("Switch to window: "+UtilGl.formatMode(mode));
		}
		Display.setFullscreen(false);
		Display.setDisplayMode(mode);
	}

	
	private DisplayMode getWindowMode() {
		return windowMode;
	}

	/**
	 * Teljes képernyős módba váltás
	 * @throws LWJGLException
	 */
	protected void switchToFullScreen() throws LWJGLException {
		mode = findDisplayMode(fullscreenSize.getWidth(), fullscreenSize.getHeight(), Display.getDisplayMode().getBitsPerPixel());
		if(LOG.isDebugEnabled()){
			LOG.debug("Switch to fullscreen: "+UtilGl.formatMode(mode));
		}
		Display.setDisplayMode(mode);
		Display.setFullscreen(true);
	}
	/**
	 * Megfelelő képernyőmód keresése.
	 * 
	 * Az első találatot adja vissza, aminek a mérete a paraméterként megadott.
	 * Ha nincs ilyen találat, akkor az eredeti desktop módot adja vissza
	 * 
	 * Retrieves a displaymode, if one such is available
	 * 
	 * @param width
	 *            Required width
	 * @param height
	 *            Required height
	 * @param bpp
	 *            Minimum required bits per pixel
	 * @return
	 */
	private DisplayMode findDisplayMode(int width, int height, int bpp) throws LWJGLException {
		DisplayMode[] modes = Display.getAvailableDisplayModes();
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].getWidth() == width && modes[i].getHeight() == height) {
				return modes[i];
			}
		}
		return Display.getDesktopDisplayMode();
	}

	@Override
	public boolean isCloseRequested() {
		return Display.isCloseRequested();
	}

	@Override
	public boolean isVisible() {
		return Display.isVisible();
	}

	@Override
	public boolean isDirty() {
		return Display.isDirty();
	}

	@Override
	public void processMessages() {
		Display.processMessages();
	}

	@Override
	public void update() {
		Display.update();
	}

	@Override
	public void dispose() {
		Display.destroy();
	}

	@Override
	public IKeyboard getKeyboard() {
		return new KeyboardImplLwjgl();
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws LWJGLException {
		if(fullscreen)
		{
			switchToFullScreen();
		}else
		{
			switchToWindowMode();
		}
		this.fullscreen=fullscreen;
	}
	@Override
	public MouseImplLwjgl getMouse() {
		if(mouse==null)
		{
			try {
				mouse=new MouseImplLwjgl(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return mouse;
	}

	@Override
	public boolean isFullScreen() {
		return fullscreen;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		// TODO Auto-generated method stub
		
	}
}
