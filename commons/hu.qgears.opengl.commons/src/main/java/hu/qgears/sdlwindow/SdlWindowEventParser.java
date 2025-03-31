package hu.qgears.sdlwindow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import hu.qgears.commons.UtilEvent;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.MouseImplCallback;

public class SdlWindowEventParser {
	ByteBuffer eventBuffer;
	public ESdlWindowEventType type;
	public final MouseImplCallback mouse=new MouseImplCallback();
	/**
	 * Event callback called from processEvents() function.
	 */
	public final UtilEvent<SdlWindowEventParser> windowEvent=new UtilEvent<SdlWindowEventParser>();
	public SdlWindowEventParser()
	{
		eventBuffer=ByteBuffer.allocateDirect(1024);
		eventBuffer.order(ByteOrder.nativeOrder());
	}
	public boolean parse() {
		eventBuffer.clear();
		eventBuffer.limit(eventBuffer.capacity());
		
		int eventType=eventBuffer.getInt();
		type=ESdlWindowEventType.values()[eventType];
		switch (type) {
		case windowCloseRequest:
			windowEvent.eventHappened(this);
			break;
		case mouseDown:
		{
			int x=eventBuffer.getInt();
			int y=eventBuffer.getInt();
			mouse.addEvent(4, x, y, EMouseButton.LEFT, 0);
			return false;
		}
		case mouseUp:
		{
			int x=eventBuffer.getInt();
			int y=eventBuffer.getInt();
			mouse.addEvent(4, x, y, EMouseButton.LEFT, 1);
			return false;
		}
		case none:
		default:
			System.err.println("Unknown event type: "+eventType);
			System.err.println("Unknown event type: "+eventType);
			return false;
		}
		return true;
	}
}
