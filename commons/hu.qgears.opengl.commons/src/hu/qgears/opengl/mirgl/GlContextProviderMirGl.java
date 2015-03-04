package hu.qgears.opengl.mirgl;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import lwjgl.standalone.BaseAccessor;

import org.lwjgl.opengl.GLContext;

public class GlContextProviderMirGl implements IGlContextProvider
{
	private SizeInt clientAreaSize=new SizeInt(1,1);
	private MirGl mirgl;
	private boolean dirty;
	@Override
	public void loadNatives() {
		mirgl=new MirGl(this);
		BaseAccessor.initLwjglNatives();
	}

	@Override
	public SizeInt getClientAreaSize() {
		return clientAreaSize;
	}

	@Override
	public void init() {
		mirgl.init();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle,
			SizeInt size) throws Exception {
		mirgl.openWindow(initFullscreen, initTitle,
				0,0, size.getWidth(), size.getHeight());
		clientAreaSize=size;
		GLContext.useContext(mirgl);
		mirgl.showWindow();
	}

	@Override
	public boolean isCloseRequested() {
		return mirgl.isCloseRequested();
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
		mirgl.mainLoop();
	}

	@Override
	public void update() {
		dirty=false;
		processMessages();
		mirgl.swapBuffers();
	}

	@Override
	public void dispose() {
		mirgl.dispose();
		mirgl=null;
	}
	@Override
	public IKeyboard getKeyboard() {
		return mirgl.getKeyboard();
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
		return mirgl.getMouse();
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
