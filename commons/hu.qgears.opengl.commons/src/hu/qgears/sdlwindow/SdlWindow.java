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
	private SdlWindowEventParser eventParser;
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
		eventParser=new SdlWindowEventParser();
	}
	public void updateFrame(NativeImage im) {
		if(ENativeImageComponentOrder.ARGB!=im.getComponentOrder())
		{
			throw new IllegalArgumentException("Image format not supported: "+im.getComponentOrder());
		}
		n.updateFrame(im.getBuffer().getJavaAccessor());
	}
	public void processEvents() {
		
		while(n.pollEvent(eventParser.eventBuffer))
		{
			try {
				eventParser.parse();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void openWindow(int width, int h, String windowName) {
		n.openWindow(width, h, windowName);
	}
	public void closeWindow() {
		n.closeWindow();
	}
	public SdlWindowEventParser getEventParser() {
		return eventParser;
	}
}
