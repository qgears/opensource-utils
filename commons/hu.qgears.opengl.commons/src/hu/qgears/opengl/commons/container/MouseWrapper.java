package hu.qgears.opengl.commons.container;

import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.GlMouseEvent;
import hu.qgears.opengl.commons.input.IMouse;

public class MouseWrapper implements IMouse {
	boolean active;
	IMouse mouse0;
	private GlMouseEvent lastEvent;
	OpenGLAppContainer openGLAppContainer;
	public MouseWrapper(OpenGLAppContainer openGLAppContainer) {
		this.openGLAppContainer=openGLAppContainer;
	}
	public void setActive(boolean active)
	{
		this.active=active;
		if(!active)
		{
			lastEvent=new GlMouseEvent();
			lastEvent.button=EMouseButton.LEFT;
			lastEvent.buttonState=false;
			lastEvent.nanoseconds=System.nanoTime();
			lastEvent.x=getX();
			lastEvent.y=getY();
		}
	}
	@Override
	public GlMouseEvent getNextEvent() {
		if(active)
		{
			return getMouse().getNextEvent();
		}else
		{
			if(lastEvent!=null)
			{
				GlMouseEvent ret=lastEvent;
				lastEvent=null;
				return ret;
			}
			return null;
		}
	}
	@Override
	public void poll() {
		if(active)
		{
			getMouse().poll();
		}
	}
	@Override
	public boolean isButtonDown(EMouseButton b) {
		return getMouse().isButtonDown(b);
	}
	@Override
	public int getX() {
		return getMouse().getX();
	}
	@Override
	public int getY() {
		return getMouse().getY();
	}
	public IMouse getMouse() {
		if(mouse0==null)
		{
			mouse0=openGLAppContainer.frame.getMouseObject();
		}
		return mouse0;
	}
	
	@Override
	public void addEvent(int type, int x, int y, EMouseButton button, int state) {
		mouse0.addEvent(type, x, y, button, state);
	}

}
