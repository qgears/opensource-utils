package hu.qgears.opengl.kms;

import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.NativeImageEditor;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.RGBAColor;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.libinput.Libinput;
import hu.qgears.opengl.libinput.LibinputEvent;
import hu.qgears.opengl.libinput.LibinputInstance;
import hu.qgears.opengl.libinput.LibinputKeyboard;
import hu.qgears.opengl.osmesa.OSMesa;
import hu.qgears.opengl.osmesa.OSMesaInstance;
import lwjgl.standalone.BaseAccessor;

public class GlContextProviderOsMesaKMS implements IGlContextProvider
{
	private KMSMouse mouse=new KMSMouse();
	LibinputKeyboard keyboard=new LibinputKeyboard();
	private boolean exit=false;
	private Libinput li;
	private KMS kms;
	private OSMesa osMesa;
	private SizeInt size=new SizeInt(0, 0);

	@Override
	public void loadNatives() {
		OSMesaInstance.getInstance();
		BaseAccessor.headless=true;
		BaseAccessor.initLwjglNatives();
		UtilGl.flipY=true;
		KMSInstance.getInstance();
		LibinputInstance.getInstance();
	}

	@Override
	public SizeInt getClientAreaSize() {
		return size;
	}

	@Override
	public void init() {
		li=new Libinput();
		li.keyboard.addListener(new UtilEventListener<LibinputEvent>() {
			
			@Override
			public void eventHappened(LibinputEvent msg) {
				if(msg.a==1)
				{
					exit=true;
				}
			}
		});
		kms=new KMS();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle, SizeInt size) throws Exception {
		kms.enterKmsFullscreen();
		this.size=kms.getCurrentBackBuffer().getSize();
		mouse.init(size, li);
		osMesa=new OSMesa();
		osMesa.createContext(ENativeImageComponentOrder.BGRA);
		this.size=size;
		osMesa.makeCurrent(kms.getCurrentBackBuffer());
		System.out.println("OSMesa OpenGL Version: '"+osMesa.getGlVersion()+"'");
		GLContext.useContext(osMesa);
	}

	@Override
	public boolean isCloseRequested() {
		return exit;
	}

	@Override
	public boolean isVisible() {
		// TODO feedback switch of console to other application
		return true;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void processMessages() {
		li.poll();
	}

	@Override
	public void update() {
		NativeImage im=kms.getCurrentBackBuffer();
		if(mouse.isButtonDown(EMouseButton.LEFT))
		{
			NativeImageEditor ed=new NativeImageEditor(im);
			ed.fillRect(mouse.getX(), mouse.getY(), 10, 10, RGBAColor.RED);
		}
		im.setPixel(mouse.getX(), mouse.getY(), RGBAColor.WHITE);
		kms.swapBuffers();
		li.poll();
		osMesa.makeCurrent(kms.getCurrentBackBuffer());
	}

	@Override
	public void dispose() {
		try
		{
			osMesa.disposeContext();
		}catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		osMesa=null;
		try {
			li.dispose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		li=null;
		try {
			kms.dispose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		kms=null;
	}

	@Override
	public IKeyboard getKeyboard() {
		return keyboard;
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws Exception {
		// Not implemented
	}

	@Override
	public boolean isFullScreen() {
		return true;
	}

	@Override
	public IMouse getMouse() {
		return mouse;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		// TODO Not implemented: vsync is always on
	}

}
