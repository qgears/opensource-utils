package hu.qgears.opengl.x11;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import lwjgl.standalone.BaseAccessor;

import org.lwjgl.opengl.GLContext;

public class GlContextProviderX11 implements IGlContextProvider
{
	private SizeInt clientAreaSize=new SizeInt(1,1);
	private X11Gl x11;
	private boolean dirty;
	@Override
	public void loadNatives() {
		x11=new X11Gl(this);
		BaseAccessor.initLwjglNatives();
	}

	@Override
	public SizeInt getClientAreaSize() {
		return clientAreaSize;
	}

	@Override
	public void init() {
		x11.init();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle,
			SizeInt size) throws Exception {
		x11.openWindow(initFullscreen, initTitle,
				0,0, size.getWidth(), size.getHeight());
		clientAreaSize=size;
		GLContext.useContext(x11);
		x11.showWindow();
	}

	@Override
	public boolean isCloseRequested() {
		return x11.isCloseRequested();
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void processMessages() {
		x11.mainLoop();
	}

	@Override
	public void update() {
		dirty=false;
		processMessages();
		x11.swapBuffers();
	}

	@Override
	public void dispose() {
		x11.dispose();
		x11=null;
	}
	@Override
	public IKeyboard getKeyboard() {
		return x11.getKeyboard();
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFullScreen() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public IMouse getMouse() {
		return x11.getMouse();
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		// TODO Auto-generated method stub
		
	}

	public void callbackResize(int width, int height) {
		dirty=true;
		if(width!=-1&&height!=-1)
		{
			clientAreaSize=new SizeInt(width, height);
		}
	}
}
