package hu.qgears.opengl.x11;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.opengl.commons.OGlGlobalParameters;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;

/**
 * This class is only accessible from its package.
 * @author rizsi
 *
 */
class X11Gl {
	GlContextProviderX11 parent;
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
	private MouseImplCallback mouse=new MouseImplCallback();
	private KeyboardImplX11 keyboard=new KeyboardImplX11();
	native public void mainLoop();
	native public void dispose();
	native public boolean isCloseRequested();
	
	private void callbackMouse(int press, int x, int y, int button)
	{
		int type;
		int buttonUp;
		switch(press)
		{
		case 0:
			buttonUp=1;
			type=4;
			break;
		case 1:
			buttonUp=0;
			type=4;
			break;
		case 2:
			buttonUp=1;
			type=5;
			break;
		default:
			throw new RuntimeException("Invalid mouse press type");
		}
		EMouseButton eButton=convertButtonId(button);
		if(OGlGlobalParameters.logMouseMessages)
		{
			System.out.println("X11 Mouse: "+button);
		}
		mouse.addEvent(type, x, y, eButton, buttonUp);
	}
	private EMouseButton convertButtonId(int button) {
		switch (button) {
		case 1:
			return EMouseButton.LEFT;
		case 2:
			return EMouseButton.MIDDLE;
		case 3:
			return EMouseButton.RIGHT;
		case 4:
			return EMouseButton.WHEEL_UP;
		case 5:
			return EMouseButton.WHEEL_DOWN;
		default:
			break;
		}
		return null;
	}
	private void callbackKeyboard(int press, int x, int y, int keyCode, int state, int unicode)
	{
		keyboard.addEvent(press, x, y, keyCode, state, unicode);
	}
	private void callbackResize(int width, int height)
	{
		parent.callbackResize(width, height);
	}
	public IKeyboard getKeyboard() {
		return keyboard;
	}
	public IMouse getMouse() {
		return mouse;
	}
}
