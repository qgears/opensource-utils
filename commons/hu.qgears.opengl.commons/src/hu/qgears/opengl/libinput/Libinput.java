package hu.qgears.opengl.libinput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import hu.qgears.commons.UtilEvent;

public class Libinput {
	private LibinputNative n;
	private ByteBuffer bb;
	private int strip;
	public final UtilEvent<LibinputEvent> event=new UtilEvent<LibinputEvent>();
	public final UtilEvent<LibinputEvent> keyboard=new UtilEvent<LibinputEvent>();
	public final UtilEvent<LibinputEvent> pointer=new UtilEvent<LibinputEvent>();
	private boolean away;
	public Libinput()
	{
		LibinputAccessor.getInstance();
		n=new LibinputNative();
		n.init();
		bb=n.getInputBuffer();
		bb.order(ByteOrder.nativeOrder());
		strip=n.getInputBufferStrip();
		System.out.println("Strip: "+strip);
	}
	public void dispose() {
		n.dispose();
	}
	private LibinputEvent ev=new LibinputEvent();
	public void poll() {
		int N=n.poll();
		bb.clear();
		bb.limit(strip*N);
		if(!away)
		{
			for(int i=0;i<N;++i)
			{
				bb.position(strip*i);
				ELibinputEventType type=ELibinputEventType.byOrdinal(bb.getInt());
				ev.type=type;
				ev.a=bb.getInt();
				ev.b=bb.getInt();
				ev.c=bb.getInt();
				ev.da=bb.getDouble();
				ev.db=bb.getDouble();
				event.eventHappened(ev);
				switch(type)
				{
				case key:	// keyboard
					keyboard.eventHappened(ev);
					break;
				case pointerMotion: // Pointer motion
				case pointerAbsolute: // Pointer motion
				case pointerButton: // Pointer motion
					pointer.eventHappened(ev);
					break;
				}
			}
		}
	}
	public void switchedAway(boolean away) {
		this.away=away;
	}
}
