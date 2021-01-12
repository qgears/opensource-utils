package hu.qgears.opengl.mirgl;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;

class MirGl {
	private GlContextProviderMirGl parent;
	private MirKeyboard keyboard=new MirKeyboard();
	private MouseImplCallback mouse=new MouseImplCallback();
	protected MirGl(GlContextProviderMirGl parent)
	{
		this.parent=parent;
		UtilNativeLoader.loadNatives(new MirGlAccessor());
	}
	private int buttonMask;

	native protected void init();
	native public void openWindow(boolean initFullscreen, String initTitle, int i,
			int j, int width, int height);

	native public void showWindow();
	native public boolean isCloseRequested();
	native public void mainLoop();
	native public void swapBuffers();
	
	public void dispose() {
		// TODO Auto-generated method stub
	}
	public IKeyboard getKeyboard() {
		return keyboard;
	}
	public IMouse getMouse() {
		return mouse;
	}

}
