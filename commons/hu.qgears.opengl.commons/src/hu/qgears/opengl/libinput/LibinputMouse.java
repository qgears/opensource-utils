package hu.qgears.opengl.libinput;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.MouseImplCallback;

/**
 * Libinput fullscreen mouse implementation
 */
public class LibinputMouse extends MouseImplCallback
{
	private double x=0;
	private double y=0;
	public LibinputMouse()
	{
	}
	public void init(final SizeInt size, Libinput input) {
		x=size.getWidth()/2;
		y=size.getHeight()/2;
		if (input!=null)
		{
		input.pointer.addListener(new UtilEventListener<LibinputEvent>() {
			@Override
			public void eventHappened(LibinputEvent msg) {
				switch(msg.type)
				{
				case pointerMotion:
					x+=msg.da;
					y+=msg.db;
					if(x<0)x=0;
					if(y<0)y=0;
					if(x>size.getWidth()-1)x=size.getWidth()-1;
					if(y>size.getHeight()-1)y=size.getHeight()-1;
					addEvent(0, (int)x, (int)y, null, 0);
					break;
				case pointerButton:
					EMouseButton b=null;
					switch(msg.a)
					{
					case 272:
						b=EMouseButton.LEFT;
						break;
					case 273:
						b=EMouseButton.RIGHT;
						break;
					case 274:
						b=EMouseButton.MIDDLE;
						break;
					}
					// System.out.println("Button index: "+msg.a+" state: "+msg.b);
					addEvent(4, (int)x, (int)y, b, msg.b==1?0:1);
					break;
				default:
					// Other events are not handled here
					break;
				}
			}
		});
		}
	}
}
