package hu.qgears.opengl.osmesa;

import java.net.InetSocketAddress;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.vnc.VNCServer;
import hu.qgears.images.vnc.VncEvent;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;
import hu.qgears.opengl.glut.KeyboardImplGlut;
import hu.qgears.sdlwindow.SdlWindow;
import lwjgl.standalone.BaseAccessor;

public class GlContextProviderOsMesaVncServer implements IGlContextProvider {
	OSMesa osMesa;
	SizeInt size;
	IKeyboard keyboard;
	IMouse mouse;
	NativeImage frameBuffer;
	VNCServer vncServer;
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
		osMesa=new OSMesa();
		osMesa.createContext();
		this.size=size;
		frameBuffer=NativeImage.create(size, ENativeImageComponentOrder.ARGB, DefaultJavaNativeMemoryAllocator.getInstance());
		osMesa.makeCurrent(frameBuffer);
		GLContext.useContext(osMesa);
		vncServer=new VNCServer(size);
		vncServer.start(new InetSocketAddress("localhost", 5009), true);
		mouse=new MouseImplCallback();
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
	private int pointerPrevButtonState=0;

	@Override
	public void processMessages() {
		VncEvent ev;
		while((ev=vncServer.pollEvent())!=null)
		{
			if(ev.isPointer())
			{
				int mask=ev.getPointerButtonMask();
				int x=ev.getPointerX();
				int y=ev.getPointerY();
				int diff=pointerPrevButtonState^mask;
				boolean upOrDown=false;
				if((diff&1)!=0)
				{
					int status=((mask&1)==1)?0:1;
					mouse.addEvent(4, x, y, EMouseButton.LEFT, status);
					upOrDown=true;
				}
				if((diff&2)!=0)
				{
					mouse.addEvent(4, x, y, EMouseButton.MIDDLE, ((mask&2)==1)?0:1);
					upOrDown=true;
				}
				if((diff&4)!=0)
				{
					mouse.addEvent(4, x, y, EMouseButton.RIGHT, ((mask&4)==1)?0:1);
					upOrDown=true;
				}
				if(!upOrDown)
				{
					mouse.addEvent(0, x, y, null, 0);
				}
				pointerPrevButtonState=mask;
			}else
			{
				// TODO other messages are ignored
			}
		}
	}

	@Override
	public void update() {
		vncServer.updateFrame(frameBuffer);
		try {
			Thread.sleep(50); // Max 20 FPS
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		vncServer.dispose();
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
