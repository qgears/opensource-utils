package hu.qgears.opengl.commons.container;

import java.util.List;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.IMouse;

public class OpenGLAppContainer implements IOGLContainer {
	NativeImage previousImage;

	protected OpenGLFrame frame;
	private IOGlApplication app;
	private boolean initialized=false;
	private boolean redrawNeeded;
	MouseWrapper mouse;
	
	protected boolean isInitialized() {
		return initialized;
	}

	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public OpenGLAppContainer(OpenGLFrame frame) {
		super();
		this.frame = frame;
		this.mouse=new MouseWrapper(this);
	}

	@Override
	public void setWindowSize(SizeInt sizeInt) {
		frame.setWindowSize(sizeInt);
	}

	@Override
	public void setInitTitle(String string) {
		frame.setInitTitle(string);
	}

	protected void initialize() throws Exception {
		app.initialize();
	}

	@Override
	public SizeInt getClientAreaSize() {
		return frame.getClientAreaSize();
	}

	protected void beforeBufferSwap() {
		app.beforeBufferSwap();
	}

	protected void afterBufferSwap() {
		app.afterBufferSwap();
	}

	@Override
	public SizeInt getSize() {
		return frame.getSize();
	}

	@Override
	public void setFullScreen(boolean b) throws Exception {
		frame.setFullScreen(b);
	}

	@Override
	public IMouse getMouseObject() {
		return mouse;
	}

	protected void render() {
		app.render();
	}

	protected void logic() {
		app.logic();
	}

	protected boolean isDirty() {
		boolean ret=redrawNeeded||app.isDirty();
		redrawNeeded=false;
		return ret;
	}
	
	public void requireRedraw()
	{
		redrawNeeded=true;
	}

	@Override
	public void setThisApplication(IOGlApplication app) {
		if(this.app==null)
		{
			this.app=app;
		}
		frame.addApp(this);
	}
	protected void keyDown(int eventKey, char ch, boolean shift, boolean ctrl,
			boolean alt, boolean special) throws Exception {
		app.keyDown(eventKey, ch, shift, ctrl, alt, special);
	}

	@Override
	public IOGLContainer nextApplication() {
		return frame.nextApplication();
	}
	@Override
	public IOGLContainer thisApplication() {
		return frame.selectApplication(this);
	}

	public void setActive(boolean b) {
		mouse.setActive(b);
	}

	@Override
	public NativeImage getPreviousImage() {
		return previousImage;
	}

	@Override
	public void setPreviousImage(NativeImage previousImage) {
		this.previousImage = previousImage;
	}

	@Override
	public void exit() {
		frame.exit();		
	}

	@Override
	public void dispose() {
		frame.remove(this);
	}

	@Override
	public void setVSyncEnabled(boolean curr) {
		frame.setVSyncEnabled(curr);
	}

	@Override
	public List<IOGLContainer> getApplications() {
		return frame.getApplications();
	}

	@Override
	public void setActiveApplication(IOGLContainer application) {
		frame.selectApplication((OpenGLAppContainer)application);
	}
	@Override
	public IOGLContainer getActiveApplication() {
		return frame.getActiveApplication();
	}
	@Override
	public boolean isFullscreen() {
		return frame.isFullscreen();
	}

	@Override
	public boolean isActive() {
		return frame.getActiveApplication()==this;
	}
	
	@Override
	public IOGlApplication getThisApplication() {
		return app;
	}
}
