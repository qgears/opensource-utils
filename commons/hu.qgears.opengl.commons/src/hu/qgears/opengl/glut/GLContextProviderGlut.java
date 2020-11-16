package hu.qgears.opengl.glut;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.OGlGlobalParameters;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;
import lwjgl.standalone.BaseAccessor;

public class GLContextProviderGlut implements IGlContextProvider{
	
	private static final Logger LOG = LogManager.getLogger(
			GLContextProviderGlut.class);
	
	private Glut glut;
	private ByteBuffer messagesBuffer;
	private SizeInt size;
	private SizeInt prevSize=new SizeInt(0, 0);
	private boolean dirty;
	@Override
	public void loadNatives() {
		GlutInstance.getInstance();
		BaseAccessor.initLwjglNatives();
	}
	
	@Override
	public void init()
	{
		glut=new Glut();
		glut.init();
		messagesBuffer=glut.getMessagesBuffer();
		messagesBuffer.order(ByteOrder.nativeOrder());
	}

	@Override
	public SizeInt getClientAreaSize() {
		return prevSize;
	}
	private SizeInt getClientAreaSizePrivate()
	{
		// TODO optimize to execute native query only when something has changed
		if(fullscreen)
		{
			checkSizeChange(glut.getScreenWidth(), glut.getScreenHeight());
		}else
		{
			checkSizeChange(glut.getWindowWidth(), glut.getWindowHeight());
		}
		return prevSize;
	}
	private void checkSizeChange(int w, int h) {
		if(prevSize.getWidth()!=w||prevSize.getHeight()!=h)
		{
			dirty=true;
			prevSize=new SizeInt(w, h);
		}
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle,
			SizeInt size) throws LWJGLException {
		this.size=size;
		glut.openWindow(false, size.getWidth(), size.getHeight(), initTitle);
		if(initFullscreen)
		{
			glut.setFullScreen(initFullscreen, size.getWidth(), size.getHeight());
		}
		GLContext.useContext(glut);
		fullscreen=initFullscreen;
	}

	@Override
	public boolean isCloseRequested() {
		return glut.isCloseRequested();
	}

	@Override
	public boolean isVisible() {
		return glut.isVisible();
	}

	@Override
	public boolean isDirty() {
		return glut.isDirty()||dirty;
	}

	@Override
	public void processMessages() {
		// Read records from ringbuffer and translate them to Java event objects. 
		int N=messagesBuffer.capacity()/Glut.messageSize;
		glut.mainLoopEvent();
		int from=glut.getAndResetMessagesReadIndex();
		int to=glut.getMessagesWriteIndex();
		while(from!=to)
		{
			messagesBuffer.position(from*Glut.messageSize);
			int type=messagesBuffer.getInt();
			int x=messagesBuffer.getInt();
			int y=messagesBuffer.getInt();
			int button=messagesBuffer.getInt();
			int state=messagesBuffer.getInt();
			int characterCode = messagesBuffer.getInt();
			processMessage(type, x, y, button, state,characterCode);
			from++;
			from%=N;
		}
		getClientAreaSizePrivate();
	}

	private void processMessage(int type, int x, int y, int button, int state, int characterCode) {
		switch (type) {
		case 5:	// EVENT MOUSE MOTION
			button=-1;
			state=1;
		case 4:	// EVENT_MOUSE
			EMouseButton eButton=convertButtonId(button);
			if(OGlGlobalParameters.logMouseMessages && LOG.isInfoEnabled())
			{
				LOG.info("Glut Mouse: "+button);
			}
			mouse.addEvent(type, x, y, eButton, state);
			break;
		case 0:
		{
			characterCode = characterCode < 0 ? 0 : characterCode;
			//button and state must be between 0 and 255
			
			//|charchode 32 bit 			   | state 16 bit   | button 16 bit  |
			//|................................|................|................|
			long ev=button+(state<<16)+(((long)characterCode)<< 32);
			keyboard.addEvent(ev);
			break;
		}
		case 2:
			//|unused in case of special keys  | state 16 bit   | button 16 bit  |
			//|................................|........s.......|................|
			//the 24th LSB is the special indicator bit ↑
			long ev=button+(state<<16)+(1<<24);
			keyboard.addEvent(ev);
			break;
		default:
			break;
		}
	}

	private EMouseButton convertButtonId(int button) {
		switch(button)
		{
		case 0:
			return EMouseButton.LEFT;
		case 1:
			return EMouseButton.MIDDLE;
		case 2:
			return EMouseButton.RIGHT;
		case 3:
			return EMouseButton.WHEEL_UP;
		case 4:
			return EMouseButton.WHEEL_DOWN;
		default:
			return null;
		}
	}

	@Override
	public void update() {
		dirty=false;
		processMessages();
		glut.swapBuffers();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IKeyboard getKeyboard() {
		return keyboard;
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws Exception {
		glut.setFullScreen(fullscreen, size.getWidth(), size.getHeight());
		this.fullscreen=fullscreen;
	}
	KeyboardImplGlut keyboard=new KeyboardImplGlut();
	MouseImplCallback mouse=new MouseImplCallback();
	@Override
	public IMouse getMouse() {
		return mouse;
	}

	private boolean fullscreen;
	@Override
	public boolean isFullScreen() {
		return fullscreen;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		glut.setupVSync2(vSyncEnabled?1:0);
	}
}
