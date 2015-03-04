package hu.qgears.opengl.mirgl;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.opengl.commons.OGlGlobalParameters;
import hu.qgears.opengl.commons.input.EMouseButton;
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
	private void callbackMouse(int press, int x, int y, int button)
	{
		int type;
		int buttonUp;
		int buttonPressed;
		switch(press)
		{
		case 0:
			// up action
			buttonUp=1;
			type=4;
			buttonPressed=buttonMask;
			buttonMask=0;
			break;
		case 1:
			// down action
			buttonPressed=button;
			buttonMask=button;
			buttonUp=0;
			type=4;
			break;
		case 2:
			// move action
			buttonUp=1;
			type=5;
			buttonPressed=button;
			break;
		default:
			throw new RuntimeException("Invalid mouse press type");
		}
		EMouseButton eButton=convertButtonId(buttonPressed);
		if(OGlGlobalParameters.logMouseMessages)
		{
			System.out.println("MIR Mouse: "+press+" "+button);
		}
		mouse.addEvent(type, x, y, eButton, buttonUp);
	}
	private EMouseButton convertButtonId(int button) {
		switch (button) {
		case 1:
			return EMouseButton.LEFT;
//		case 2:
//			return EMouseButton.MIDDLE;
//		case 3:
//			return EMouseButton.RIGHT;
//		case 4:
//			return EMouseButton.WHEEL_UP;
//		case 5:
//			return EMouseButton.WHEEL_DOWN;
		default:
			break;
		}
		return null;
	}
	private void callbackKeyboard(int press, int x, int y, int keyCode, int state, int unicode)
	{
		keyboard.addEvent(press, x, y, keyCode, state, unicode);
//		if(press==1)
//		{
//			if(OGlGlobalParameters.logKeyMessages)
//			{
//				System.out.println("press: "+press+" "+keyCode+" "+unicode+" "+(char)unicode);
//			}
//		}
	}
	private void callbackResize(int width, int height)
	{
		parent.callbackResize(width, height);
	}

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
