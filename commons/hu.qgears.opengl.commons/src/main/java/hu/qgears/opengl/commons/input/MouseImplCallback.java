package hu.qgears.opengl.commons.input;

import org.apache.log4j.Logger;

import hu.qgears.opengl.commons.OGlGlobalParameters;

/**
 * Common mouse implementation between GLUT and X11
 * implementation.
 * @author rizsi
 *
 */
public class MouseImplCallback implements IMouse {
	private static final Logger LOG = Logger.getLogger(MouseImplCallback.class);

	private int globalState=0;
	private int x;
	private int y;
	
	private GlMouseEvent[] events=new GlMouseEvent[1024];
	int eventWritePtr=0;
	int eventReadPtr=0;

	public MouseImplCallback() {
		super();
		for(int i=0;i<events.length;++i)
		{
			events[i]=new GlMouseEvent();
		}
	}

	@Override
	public void poll() {
	}

	@Override
	public boolean isButtonDown(EMouseButton b) {
		if(b!=null)
		{
			int mask=1<<b.ordinal();
			return (globalState&mask)!=0;
		}
		return false;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEvent(int type, int x, int y, EMouseButton button, int state) {
		boolean buttonDown=state!=1;
		GlMouseEvent currEv=events[eventWritePtr];
		currEv.clear();
		currEv.x=x;
		currEv.y=y;
		if(OGlGlobalParameters.logMouseMessages && LOG.isDebugEnabled())
		{
			LOG.debug("Mouse ev: t: "+type+" b: "+button+" button down: "+buttonDown+" ("+x+","+y+")");
		}
		if(type==4&&button!=null)
		{
			int mask=1<<button.ordinal();
			if(!buttonDown)
			{
				globalState&=~mask;
			}else
			{
				globalState|=mask;
			}
			currEv.button=button;
			currEv.buttonState=buttonDown;
		}else
		{
			currEv.button=null;
		}
		
		this.x=x;
		this.y=y;
		eventWritePtr++;
		eventWritePtr%=events.length;
	}

	@Override
	public GlMouseEvent getNextEvent() {
		if(eventReadPtr!=eventWritePtr)
		{
			GlMouseEvent ret=events[eventReadPtr];
			eventReadPtr++;
			eventReadPtr%=events.length;
			return ret;
		}
		return null;
	}

}
