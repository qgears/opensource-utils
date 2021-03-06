package hu.qgears.opengl.osmesa;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.glut.KeyboardImplGlut;
import hu.qgears.sdlwindow.SdlWindow;
import hu.qgears.sdlwindow.SdlWindowEventParser;
import lwjgl.standalone.BaseAccessor;

public class GlContextProviderOsMesaSdl implements IGlContextProvider {
	OSMesa osMesa;
	SizeInt size;
	IKeyboard keyboard;
	IMouse mouse;
	SdlWindow window;
	NativeImage frameBuffer;
	private boolean closeRequested=false;

	@Override
	public void loadNatives() {
		OSMesaInstance.getInstance();
		BaseAccessor.initLwjglNatives();
		SdlWindow.loadNatives();
		UtilGl.flipY=true;
	}

	@Override
	public SizeInt getClientAreaSize() {
		return size;
	}

	@Override
	public void init() {
		System.out.println("OSMesa Inited Thread: "+Thread.currentThread().getId());
		keyboard=new KeyboardImplGlut();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle, final SizeInt size) throws Exception {
		System.out.println("OSMesa SDL Window opened: "+Thread.currentThread().getId());
		ENativeImageComponentOrder co=ENativeImageComponentOrder.ARGB;
		osMesa=new OSMesa();
		osMesa.createContext(co);
		this.size=size;
		frameBuffer=NativeImage.create(size, co, DefaultJavaNativeMemoryAllocator.getInstance());
		osMesa.makeCurrent(frameBuffer);
		GLContext.useContext(osMesa);
		window=new SdlWindow();
		mouse=window.getEventParser().mouse;
		window.getEventParser().windowEvent.addListener(new UtilEventListener<SdlWindowEventParser>() {
			
			@Override
			public void eventHappened(SdlWindowEventParser msg) {
				switch (msg.type) {
				case windowCloseRequest:
					closeRequested=true;
					break;

				default:
					break;
				}
			}
		});
		window.openWindow(size.getWidth(), size.getHeight(), initTitle);
	}
	@Override
	public boolean isCloseRequested() {
		return closeRequested;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processMessages() {
		window.processEvents();
	}

	@Override
	public void update() {
		window.updateFrame(frameBuffer);
		processMessages();
	}

	@Override
	public void dispose() {
		try {
			GLContext.useContext(null);
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		osMesa.disposeContext();
		window.closeWindow();
	}

	@Override
	public IKeyboard getKeyboard() {
		return keyboard;
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws Exception {
		// Not implemented in Swing implementation
	}

	@Override
	public boolean isFullScreen() {
		// Not implemented in Swing implementation
		return false;
	}

	@Override
	public IMouse getMouse() {
		return mouse;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		// Not implemented in Swing implementation
	}

}
