package hu.qgears.opengl.commons;


import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.lwjgl.GLContextProviderLwjgl;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import org.lwjgl.util.glu.GLU;


/**
 * Base class of OpenGL window applications
 * 
 * @author rizsi
 *
 */
public abstract class AbstractOpenglApplication2 {
	
	private static final Logger LOG = Logger
			.getLogger(AbstractOpenglApplication2.class);
	
	private EGLImplementation implementation=EGLImplementation.lwjgl;
	protected FpsCounter fpsCounter=new FpsCounter();
	private SizeInt windowSize=new SizeInt(640, 480);
	public void setWindowSize(SizeInt windowSize) {
		this.windowSize = windowSize;
	}
	private SizeInt size=windowSize;
	private boolean initFullscreen=false;
	private boolean wireFrame;
	private boolean exit=false;
	private String initTitle="";

	public void setInitTitle(String initTitle) {
		this.initTitle = initTitle;
	}
	/**
	 * Az alkalmazás végrehajtása. A main metódusból kell meghívni.
	 * @throws Exception 
	 */
	public void execute() throws Exception {
		try {
			try {
				initialize();
				mainLoop();
			} catch (Exception e) {
				LOG.error("Unexpected exception during execute", e);
				throw e;
			} catch (Throwable e) {
				LOG.error("Unexpected exception during execute", e);
				throw new Exception(e);
			}
		} finally {
			cleanup();
		}
	}
	private IGlContextProvider glprovider=new GLContextProviderLwjgl();
	private IKeyboard keyboard;
	/**
	 * Az alkalmazás inicializálása.
	 * @throws Exception 
	 */
	protected void initialize() throws Exception {
		glprovider=implementation.createProvider();
		glprovider.loadNatives();
		glprovider.init();
		keyboard=glprovider.getKeyboard();
		size=windowSize;
		glprovider.openWindow(initFullscreen, initTitle, size);
	}
	/**
	 * A fő ciklus futtatása. Ez a ciklus fut a végtelenségig,
	 * illetve amig le nem állítjuk az alkalmazást
	 * @throws Exception 
	 */
	private void mainLoop() throws Exception {
		// Ameddig nem nyom a felhasználó ESC-t vagy meg nem nyomja az ablak bezárás gombját
		while (!exit
				&& !glprovider.isCloseRequested()) {
			// Ha látható a képernyő, akkor 
			if (glprovider.isVisible()) {
				doFrame(true);
			} else {
				doFrame(false);
				// altassuk a szálat egy ideig, hogy ne pörgesse a processz
				// a CPU-t 100%-on.
				try {
					Thread.sleep(100);
				} catch (InterruptedException inte) {
				}
			}
		}
	}
	/**
	 * Is the application state dirty?
	 *  * No means the screen need not be redrawn
	 *  * Yes means the screen must be redrawn
	 * After the query the framework guarantees that 
	 * draw will be called so the application can clear
	 * the isDirty flag on the call at once. 
	 * @return
	 */
	protected abstract boolean isDirty();
	private void doFrame(boolean needRedraw) throws Exception {
		afterBufferSwap();
		needRedraw=doCycle(needRedraw);
		if(needRedraw)
		{
			GL11.glFlush();
			beforeBufferSwap();
			// Erre a hívásra fogja az lwjgl a képernyőképet frissíteni
			// Ebben a lépésben van várakozás a vsync miatt
			// (a monitor képfrissítési frekvenciájánál több képet nem állítunk elő)
			// ,ezért nem fog
			// 100%-on futni a CPU
			glprovider.update();
		}
		else
		{
			glprovider.processMessages();
		}
	}
	protected void beforeBufferSwap() {
	}
	protected void afterBufferSwap() {
	}
	protected boolean doCycle(boolean needRedraw) throws Exception
	{
		// billentyűzet események feldolgozása
		processKeyboard(keyboard);
		// A játék logikáját léptetjük
		logic();
		
		boolean appDirty=isDirty();
		needRedraw=needRedraw&&(glprovider.isDirty()||
				appDirty);

		// kirajzoljuk a játékteret
		if(needRedraw)
		{
			render();
			fpsCounter.frameDrawn();
		}else
		{
			try {
				Thread.sleep(1000/60);
			} catch (InterruptedException e) {
			}
		}
		return needRedraw;
	}
	/**
	 * Processing keyboard events.
	 * 
	 * The basic functionality is:
	 *  * read keyboard events.
	 *  * in case of key down call keyDown method
	 *  * in case of ESC key down set exit to true
	 *  
	 * Can be overridden by subclasses.
	 * @throws Exception 
	 */
	protected void processKeyboard(IKeyboard keyboard) throws Exception
	{
		while(keyboard.next())
		{
			if(keyboard.isKeyDown())
			{
				try {
					keyDown(keyboard.getEventKey(), keyboard.getEventCharacter(), keyboard.isShift(), keyboard.isCtrl(), keyboard.isAlt(), keyboard.isSpecialKey());
				} catch (Exception e) {
					logError("Error processing keyboard event", e);
				}
			}
		}
	}
	protected abstract void logError(String message, Exception e);
	/**
	 * Akkor hívjuk meg a metódust, amikor egy billentyű lenyomást érzékel
	 * az alkalmazás.
	 * 
	 * Default functions:
	 *  "1" fullscreen
	 *  "2" windowed
	 *  "v" toggle wireframe
	 * 
	 * @param eventKey
	 * @param ch
	 * @throws LWJGLException
	 */
	protected void keyDown(int eventKey, char ch, boolean shift, boolean ctrl, boolean alt,
			boolean special) throws Exception
	{
		switch(ch)
		{
		case '1':
			setFullScreen(true);
			break;
		case '2':
			setFullScreen(false);
			break;
		case 'v':
			if (LOG.isInfoEnabled()) {
				LOG.info("Wireframe toggled!");
			}
			wireFrame=!wireFrame;
			break;
		}
	}

	public void setFullScreen(boolean fullscreen) throws Exception {
		glprovider.setFullScreen(fullscreen);
	}
	public boolean isFullscreen() {
		return glprovider.isFullScreen();
	}
	/**
	 * A képernyőkép előállítása OpenGL hívások segítségével.
	 */
	protected abstract void render();
	/**
	 * Az alkalmazás logika léptetése.
	 */
	protected abstract void logic();
	/**
	 * Stop the application.
	 * Request to close the window, OpenGL thread and free resources.
	 */
	public void exit() {
		exit=true;
	}
	/**
	 * Az allokált erőforrások felszabadítása a program leállításakor.
	 */
	protected void cleanup() {
		glprovider.dispose();
	}
	/**
	 * Initialize a 2d mapping to the view:
	 *
	 * (0,0) is the middle of the view
	 * 1 pixel is 1 in the model
	 * x is directed right, y is directed up
	 */
	protected void initOrtho2d(SizeInt size) {
		UtilGl.initOrtho2d(size);
	}
	/**
	 * A nézeti és a model-view mátrixok inicializásása.
	 * 
	 * Elhelyezi a kamerát a 0,0 koordinátára és a z tengely irányába nézünk. Az y tengely van felfelé.
	 * 
	 * A legközelebbi látható pont a z=1 síkon van, a legtávolabbi a z=10000 síkon
	 * 
	 */
	protected void glInit3D(int width, int height) {
		glInit3D(width, height, 30.0f, 1.0f);
	}
	/**
	 * A nézeti és a model-view mátrixok inicializásása.
	 * 
	 * Elhelyezi a kamerát a 0,0 koordinátára és a z tengely irányába nézünk. Az y tengely van felfelé.
	 * 
	 * A legközelebbi látható pont a z=1 síkon van, a legtávolabbi a z=10000 síkon
	 * 
	 */
	protected void glInit3D(int width, int height, float fov, float aspectCorrection) {
		// OpenGL-ben általában float értékekkel számolunk mindent
		float w=width;
		float h=height;
		// Azt jelezzük az OpenGL felé, hogy a
		// projekciós (a tér síkra leképzése)
		// mátrixot (transzformációt) fogjuk állítani
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		// Egységmátrixot betöltjük, azaz nullázzuk a korábbi beállítást
		GL11.glLoadIdentity();
		// Az adott látószögű kamera beállítása
		// itt adjuk meg a képarányt és a legközelebbi, illetve
		// legtávolabbi látható pont távolságát is
		GLU.gluPerspective((float)fov, w/h*aspectCorrection, 1.0f,10000f);
		// Azt jelezzük az OpenGL felé, hogy a
		// model-view mátrixot állítjuk be
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		// egységmátrix betöltése, azaz a korábbiak törlése
		GL11.glLoadIdentity();
		// Beállítjuk, hogy a képernyő melyik részére rajzolunk
		// természetesen a teljes képernyőt használjuk
		GL11.glViewport(0, 0, width, height);
		UtilGl.setWireFrame(wireFrame);
	}
	public boolean getInitFullscreen() {
		return initFullscreen;
	}
	/**
	 * Set whether the application is inited fullscreen or not.
	 * 
	 * Must be set before calling initialize() in order to have effect.
	 * @param initFullscreen
	 */
	public void setInitFullscreen(boolean initFullscreen) {
		this.initFullscreen = initFullscreen;
	}
//	public void setFullscreenResolution(SizeInt fullscreenresolution)
//	{
//		fullscreenSize=fullscreenresolution;
//	}
//	public SizeInt getFullscreenResolution()
//	{
//		return fullscreenSize;
//	}
	/**
	 * Initializes matrixes for ortho rendering
	 */
	public void glInitOrtho(int width, int height) {
		// Go into orthographic projection mode.
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(0, width, 0, height);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		setViewPort(width, height);
	}
	public SizeInt getClientAreaSize()
	{
		return glprovider.getClientAreaSize();
	}
	private Rectangle setViewPort(int width, int height)
	{
		return UtilGl.setViewPort(width, getSize().getHeight(), getClientAreaSize().getHeight(), null);
	}
	/**
	 * Get the client area size.
	 * @return
	 */
	public SizeInt getSize() {
		return glprovider.getClientAreaSize();
	}
	/**
	 * Initializes matrixes for 3D rendering
	 */
	protected void glInit3D(int width, int height, int appWidth, int appHeight) {
		UtilGl.glInit3D(null, width, height, appWidth, appHeight);
	}
	public IMouse getMouseObject() {
		return glprovider.getMouse();
	}
	public void setImplementation(EGLImplementation implementation) {
		this.implementation = implementation;
	}
	public void setVSyncEnabled(boolean curr) {
		glprovider.setVSyncEnabled(curr);
	}
}
