package hu.qgears.opengl.lwjgl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

import hu.qgears.opengl.commons.OGlGlobalParameters;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.GlMouseEvent;
import hu.qgears.opengl.commons.input.IMouse;

public class MouseImplLwjgl implements IMouse
{
	private static final Logger LOG = LogManager.getLogger(MouseImplLwjgl.class);
	private GLContextProviderLwjgl provider;
	private GlMouseEvent event=new GlMouseEvent();
	private GlMouseEvent wheelReleaseEvent=null;
	public MouseImplLwjgl(GLContextProviderLwjgl provider) throws LWJGLException {
		Mouse.create();
		this.provider=provider;
	}

	@Override
	public void poll() {
		Mouse.poll();
	}

	@Override
	public boolean isButtonDown(EMouseButton b) {
		return Mouse.isButtonDown(invertDecodeMouse(b));
	}

	@Override
	public int getX() {
		return Mouse.getX();
	}

	@Override
	public int getY() {
		return Mouse.getY();
	}

	@Override
	public GlMouseEvent getNextEvent() {
		if(wheelReleaseEvent!=null)
		{
			GlMouseEvent ret=wheelReleaseEvent;
			wheelReleaseEvent=null;
			if(OGlGlobalParameters.logMouseMessages && LOG.isDebugEnabled())
			{
				LOG.debug("LWJGL event wheel release: "+ret);
			}
			return ret;
		}
		if(Mouse.next())
		{
			event.x=Mouse.getEventX();
			event.y=provider.getClientAreaSize().getHeight()-Mouse.getEventY();
			event.button=decodeMouse(Mouse.getEventButton());
			event.nanoseconds=Mouse.getEventNanoseconds();
			event.buttonState=Mouse.getEventButtonState();
			int dwheel=Mouse.getDWheel();
			if(event.button==null && dwheel!=0)
			{
				wheelReleaseEvent=new GlMouseEvent();
				wheelReleaseEvent.x=event.x;
				wheelReleaseEvent.y=event.y;
				wheelReleaseEvent.buttonState=false;
				wheelReleaseEvent.nanoseconds=event.nanoseconds;
				event.buttonState=true;
				if(dwheel>0)
				{
					event.button=EMouseButton.WHEEL_UP;
				}else if(dwheel<0)
				{
					event.button=EMouseButton.WHEEL_DOWN;
				}
				wheelReleaseEvent.button=event.button;
			}
			if(OGlGlobalParameters.logMouseMessages && LOG.isDebugEnabled())
			{
				LOG.debug("LWJGL event: "+event+" button: "+Mouse.getEventButton()+" dwheel: "+Mouse.getEventDWheel());
			}
			return event;
		}
		return null;
	}

	private EMouseButton decodeMouse(int eventButton) {
		switch (eventButton) {
		case 0:
			return EMouseButton.LEFT; 
		case 1:
			return EMouseButton.RIGHT; 
		case 2:
			return EMouseButton.MIDDLE; 
		default:
			break;
		}
		return null;
	}

	private int invertDecodeMouse(EMouseButton b) {
		switch(b)
		{
		case LEFT:
			return 0;
		case RIGHT:
			return 1;
		case MIDDLE:
			return 2;
		default:
			return -1;
		}
	}

	/**
	 * Does nothing in this implementation.
	 * @param type ignored
	 * @param x ignored
	 * @param y ignored
	 * @param button ignored
	 * @param state ignored
	 * @see IMouse#addEvent(int, int, int, EMouseButton, int)
	 */
	@Override
	public void addEvent(final int type, final int x, final int y, 
			final EMouseButton button, final int state) {
		// Does nothing
	}
}
