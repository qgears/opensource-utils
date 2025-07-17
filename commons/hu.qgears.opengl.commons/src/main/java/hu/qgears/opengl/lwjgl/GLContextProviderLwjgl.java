package hu.qgears.opengl.lwjgl;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.Rectangle;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import lwjgl.standalone.BaseAccessor;

public class GLContextProviderLwjgl implements IGlContextProvider{
	private static final Logger LOG = Logger
			.getLogger(GLContextProviderLwjgl.class);

	private boolean fullscreen;
	/*package*/ long window;


	private long monitor;

	private MouseImplLwjgl mouse;
	private KeyboardImplLwjgl keyboard;

	private Rectangle windowRectBeforeFullScreen;

	@Override
	public void loadNatives() {
		BaseAccessor.initLwjglNatives();
	}

	private SizeInt getFullscreenSize()
	{
		// Get the resolution of the selected monitor
		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(monitor);
		return new SizeInt(vidmode.width(),vidmode.height());
	}
	
	
	/**
	 * Returns the monitor, which the closest to the current window.
	 * Should be called only, if the window is already initialized.
	 * @return
	 */
	private long getCurrentMonitor() {
		if (window != NULL) {
			PointerBuffer monitors = GLFW.glfwGetMonitors();
			if (monitors.limit() > 1) {
				//more than one monitors
				long closestMonitor = NULL;
				try ( MemoryStack stack = MemoryStack.stackPush() ) {
					IntBuffer topB = stack.mallocInt(monitors.limit() +1); // int*
					IntBuffer leftB = stack.mallocInt(monitors.limit() +1); // int*
					GLFW.glfwGetWindowPos(window, topB, leftB);
					
					final int wTop = topB.get();
					final int wLeft = leftB.get();
					
					long dSquaredMin = Long.MAX_VALUE;
					while (monitors.hasRemaining()) {
						long m = monitors.get();
						GLFW.glfwGetMonitorPos(m, topB, leftB);
						final int mTop = topB.get();
						final int mLeft = leftB.get();
						long dSQ = (mTop -wTop) * (mTop -wTop) +  (mLeft - wLeft) * (mLeft - wLeft);
						if (dSQ < dSquaredMin) {
							dSquaredMin = dSQ;
							closestMonitor = m;
						}
					}
				}
				if (closestMonitor != NULL) {
					return closestMonitor;
				}
			}
		}
		return monitor;
	}

	@Override
	public SizeInt getClientAreaSize() {
		if(fullscreen)
		{
			return getFullscreenSize();
		}else
		{
			return getWindowSize();
		}
	}

	private SizeInt getWindowSize() {
		Rectangle r = getWindowRect();
		return new SizeInt(r.getWidth(),r.getHeight());
	}
	
	private Rectangle getWindowRect() {
		
		try ( MemoryStack stack = MemoryStack.stackPush() ) {
			IntBuffer b = stack.mallocInt(2); // int*
			IntBuffer b2 = stack.mallocInt(2); // int*
			// Get the window size passed to glfwCreateWindow
			GLFW.glfwGetWindowPos(window, b, b2); 
			int left = b.get();
			int top = b2.get();
			GLFW.glfwGetWindowSize(window, b, b2); 
			int width = b.get();
			int height = b2.get();
			return new Rectangle(left,top,width, height);
		}
		
	}

	@Override
	public void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !GLFW.glfwInit() ) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// Configure GLFW
		GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE); // the window will be resizable

	}
	@Override
	public void openWindow(boolean initFullscreen, String initTitle,
			SizeInt size) throws LWJGLException {

		//by default the window will be opened on the primary monitor
		monitor =  GLFW.glfwGetPrimaryMonitor();
		// Create the window
		if (initFullscreen) {
			window = GLFW.glfwCreateWindow(size.getWidth(), size.getHeight(), initTitle, monitor, NULL);
			fullscreen = true;
		} else {
			window = GLFW.glfwCreateWindow(size.getWidth(), size.getHeight(), initTitle, NULL, NULL);
		}
		if ( window == NULL ) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		GLFW.glfwSetKeyCallback(window, getKeyboard());
		MouseImplLwjgl m = getMouse();
		GLFW.glfwSetMouseButtonCallback(window, m::mouseButtonEvent);
		GLFW.glfwSetScrollCallback(window, m::scrollEvent);
		GLFW.glfwSetCursorPosCallback(window, m::cursorEvent);
		
		
		SizeInt fullscreenSize = getFullscreenSize();
		// Center the window
		GLFW.glfwSetWindowPos(
				window,
				(fullscreenSize.getWidth() - size.getWidth()) / 2,
				(fullscreenSize.getHeight()- size.getHeight()) / 2);

		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(window);
		// Enable v-sync
		setVSyncEnabled(true);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GLContext.useContext(this);

		// Make the window visible
		GLFW.glfwShowWindow(window);

	}
	/**
	 * Ablakos módba váltás.
	 * @throws LWJGLException
	 */
	protected void switchToWindowMode() throws LWJGLException {
		GLFW.glfwSetWindowMonitor(window, NULL, windowRectBeforeFullScreen.getX(), windowRectBeforeFullScreen.getY(),
				windowRectBeforeFullScreen.getWidth(),windowRectBeforeFullScreen.getHeight() , GLFW.GLFW_DONT_CARE);
		GLFW.glfwShowWindow(window);
	}

	/**
	 * Teljes képernyős módba váltás
	 * @throws LWJGLException
	 */
	protected void switchToFullScreen() throws LWJGLException {
		this.windowRectBeforeFullScreen = getWindowRect();
		monitor = getCurrentMonitor();
		SizeInt fullscreenSize = getFullscreenSize();
		GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, fullscreenSize.getWidth(), fullscreenSize.getHeight(),
				GLFW.GLFW_DONT_CARE);
		GLFW.glfwShowWindow(window);
	}

	@Override
	public boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(window);
	}

	@Override
	public boolean isVisible() {
		int visible = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_VISIBLE);
		return visible == 1;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void processMessages() {
		GLFW.glfwPollEvents();
	}

	@Override
	public void update() {
		processMessages();
		GLFW.glfwSwapBuffers(window);
	}

	@Override
	public void dispose() {
		// Free the window callbacks and destroy the window
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}

	@Override
	public KeyboardImplLwjgl getKeyboard() {
		if (keyboard == null) {
			keyboard = new KeyboardImplLwjgl();
		}
		return keyboard; 
	}

	@Override
	public void setFullScreen(boolean fullscreenReq) throws LWJGLException {
		if (this.fullscreen != fullscreenReq) {
			if(fullscreenReq)
			{
				switchToFullScreen();
			}else
			{
				switchToWindowMode();
			}
			this.fullscreen=fullscreenReq;
		}
	}
	@Override
	public MouseImplLwjgl getMouse() {
		if(mouse==null)
		{
			mouse=new MouseImplLwjgl(this);
		}
		return mouse;
	}

	@Override
	public boolean isFullScreen() {
		return fullscreen;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		if (vSyncEnabled) {
			// Enable v-sync
			GLFW.glfwSwapInterval(1);
		} else {
			//Disable
			GLFW.glfwSwapInterval(0);
		}
	}
}
