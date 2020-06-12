package hu.qgears.opengl.kms;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.MouseImplCallback;
import hu.qgears.opengl.libinput.Libinput;
import hu.qgears.opengl.libinput.LibinputEvent;

/**
 * KMS+libinput mouse implementation
 */
public class KMSMouse extends MouseImplCallback
{
	private SizeInt size;
	private double x=0;
	private double y=0;
	public KMSMouse(KMS kms, Libinput input) {
		size=kms.getCurrentBackBuffer().getSize();
		x=size.getWidth()/2;
		y=size.getHeight()/2;
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
