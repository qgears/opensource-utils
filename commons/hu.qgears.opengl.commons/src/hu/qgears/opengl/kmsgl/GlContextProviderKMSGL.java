package hu.qgears.opengl.kmsgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.fakejawt.FakeJawtInstance;
import hu.qgears.opengl.libinput.Libinput;
import hu.qgears.opengl.libinput.LibinputAccessor;
import hu.qgears.opengl.libinput.LibinputEvent;
import hu.qgears.opengl.libinput.LibinputKeyboard;
import hu.qgears.opengl.libinput.LibinputMouse;
import lwjgl.standalone.BaseAccessor;

public class GlContextProviderKMSGL implements IGlContextProvider
{
	private LibinputMouse mouse=new LibinputMouse();
	LibinputKeyboard keyboard=new LibinputKeyboard();
	private boolean exit=false;
	private Libinput li;
	private KMSGL kms;
	private SizeInt size=new SizeInt(0, 0);
	/**
	 * Debug feature - disable libinput totally.
	 */
	private boolean disableInput=false;

	@Override
	public void loadNatives() {
		FakeJawtInstance.getInstance();
		BaseAccessor.noX11=true;
		BaseAccessor.initLwjglNatives();
		KMSGLInstance.getInstance();
		LibinputAccessor.getInstance();
	}

	@Override
	public SizeInt getClientAreaSize() {
		return size;
	}

	@Override
	public void init() {
		if(!disableInput)
		{
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
		}
		kms=new KMSGL();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle, SizeInt size) throws Exception {
		kms.enterKmsFullscreen();
		this.size=kms.getSize();
		mouse.init(size, li);
		keyboard.init(li);
		this.size=size;
		GLContext.useContext(kms);
	}

	@Override
	public boolean isCloseRequested() {
		return exit;
	}

	@Override
	public boolean isVisible() {
		return kms.isVisible();
	}

	@Override
	public boolean isDirty() {
		return kms.isDirty();
	}

	@Override
	public void processMessages() {
		periodicTasks();
	}

	private void periodicTasks()
	{
		int vtSwRes=kms.handleVTSwitch();
		switch (vtSwRes) {
		case 0:
			// Nothing to do
			break;
		case 1:
			if(li!=null)
			{
				li.switchedAway(true);
			}
			break;
		case 2:
			// Switch back
			if(li!=null)
			{
				li.switchedAway(false);
			}
			break;
		default:
			break;
		}
		if(li!=null)
		{
			li.poll();
		}
	}
	@Override
	public void update() {
		if(mouse.isButtonDown(EMouseButton.LEFT))
		{
//			NativeImageEditor ed=new NativeImageEditor(im);
//			ed.fillRect(mouse.getX(), mouse.getY(), 10, 10, RGBAColor.RED);
		}
//		im.setPixel(mouse.getX(), mouse.getY(), RGBAColor.WHITE);
		kms.swapBuffers();
		periodicTasks();
	}

	@Override
	public void dispose() {
		try {
			GLContext.useContext(null);
		} catch (LWJGLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
