package hu.qgears.opengl.x11;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;

/**
 * This class is only accessible from its package.
 * @author rizsi
 *
 */
class X11Gl {
	private GlContextProviderX11 parent;
	private MouseImplCallback mouse=new MouseImplCallback();
	private KeyboardImplX11 keyboard=new KeyboardImplX11();
	protected X11Gl(GlContextProviderX11 parent)
	{
		this.parent=parent;
		UtilNativeLoader.loadNatives(new X11GlAccessor());
	}
	native public void openWindow(boolean initFullscreen, String initTitle,
			int x, int y,
			int width,
			int height);
	native public void init();
	native public void swapBuffers();
	native public void showWindow();
	native public void mainLoop();
	native public void dispose();
	native public boolean isCloseRequested();
	
	public IKeyboard getKeyboard() {
		return keyboard;
	}
	public IMouse getMouse() {
		return mouse;
	}
}
