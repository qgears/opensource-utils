package hu.qgears.opengl.glut;

import java.nio.ByteBuffer;

import org.lwjgl.system.FunctionProvider;

/**
 * Glut bindings accessors for Java
 * @author rizsi
 *
 */
public class Glut implements FunctionProvider {
	/**
	 * Size of the userEvent structure in bytes. See QGlut.cpp
	 * 6*sizeof(jint)=6*4
	 */
	public static final int messageSize=24;
	native public void init();
	public void openWindow(boolean fullscreen, int width, int height, String initTitle)
	{
		// TODO initTitle!
		nativeInit(fullscreen, width, height);
		setWindowTitle(initTitle);
		setupVSync2(1);
	}
	private native void nativeInit(boolean fullscreen, int width, int height);
	native public void setupVSync2(int n);
	/// Initialize Glut and run a simple test application
	native public void nativeTest();
	native public void testDrawBasicScene();
	
	/**
	 * This method must be called on each iteration of the GUI.
	 * The glut subsystem will process all messages recevied
	 * from the windowing system (Xorg).
	 * (Currently implemented events: keyboard, mouse)
	 */
	native public void mainLoopEvent();
	native public void swapBuffers();
	native public void setFullScreen(boolean fullscreen, int width, int height);
	native public int getScreenWidth();
	native public int getScreenHeight();
	native public int getWindowWidth();
	native public int getWindowHeight();
	native public void setWindowTitle(String title);
	public boolean isCloseRequested()
	{
		// TODO implement fine
		return false;
	}
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * Get the messages buffer object. 
	 * (It is a ringbuffer - see getAndResetMessagesReadIndex)
	 * The messages buffer is written by the native code of the
	 * glut JNI wrapper when mainLoopEvent() is processed.
	 * 
	 * The buffer stores records with the userEvent structure (See QGlut.cpp).
	 * In Java the records are read as integers - code must be maintained with the userEvent structure.
	 * 
	 * Possible event types: find EVENT_TYPES in QGlut.cpp
	 * 
	 * (see mainLoopEvent)
	 * @return
	 */
	native public ByteBuffer getMessagesBuffer();
	/**
	 * Messages (keyboard and mouse, see getMessagesBuffer)
	 * are stored in a ringbuffer.
	 * The ringbuffer is written by the native code with
	 * event records. The ringbuffer is red by the Java code
	 * that decodes the event records to Java event objects.
	 * 
	 * The read index is the index of the message that is
	 * to be read next. After this call the read index is set to
	 * the current write index (so the contract is that the
	 * caller has to process all messages up to currentWriteIndex
	 *  see getMessagesWriteIndex())
	 * 
	 * @return
	 */
	native public int getAndResetMessagesReadIndex();
	/**
	 * See getAndResetMessagesReadIndex
	 * @return
	 */
	native public int getMessagesWriteIndex();
	
	/**
	 * Can be used to bind LWJGL to the GL implementations used by freeglut. This is
	 * basically a JNI wrapper over "glutGetProcAddress", so the address of GL
	 * functions required for LWJGL are resolved via to the very same methods seen
	 * by freeglut.
	 * <p>
	 * This is necessary on MacOs port (however it should work on linux / windows as
	 * well).
	 */
	private native long getFunctionAddressNative(ByteBuffer functionName);
	
	@Override
	/**
	 * Can be used to bind LWJGL to the GL implementations used by freeglut. 
	 */
	public long getFunctionAddress(ByteBuffer functionName) {
		return getFunctionAddressNative(functionName);
	}
}
