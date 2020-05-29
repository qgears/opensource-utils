package hu.qgears.sdlwindow;

import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.sdlwindow.natives.SdlWindowAccessor;

/**
 * GUI Window with SDL implementation
 */
public class SdlWindow {
	static volatile SdlWindowNative n;
	public static void loadNatives()
	{
		synchronized (SdlWindow.class) {
			if(n==null)
			{
				UtilNativeLoader.loadNatives(new SdlWindowAccessor());
				n=new SdlWindowNative();
				n.init();
			}
		}
	}
	public SdlWindow() {
		loadNatives();
	}
	public void updateFrame(NativeImage im) {
		if(ENativeImageComponentOrder.BGRA!=im.getComponentOrder())
		{
			throw new IllegalArgumentException("Image format not supported: "+im.getComponentOrder());
		}
		n.updateFrame(im.getBuffer().getJavaAccessor());
	}
	public boolean processEvents() {
		return n.processEvents();
	}
	public void openWindow(int width, int h, String windowName) {
		n.openWindow(width, h, windowName);
	}
	public void closeWindow() {
		n.closeWindow();
	}
}
