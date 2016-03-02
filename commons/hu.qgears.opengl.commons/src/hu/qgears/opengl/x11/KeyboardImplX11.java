package hu.qgears.opengl.x11;

import hu.qgears.opengl.commons.OGlGlobalParameters;
import hu.qgears.opengl.commons.input.IKeyboard;

import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class KeyboardImplX11 implements IKeyboard {
	
	private static final Logger LOG = Logger.getLogger(KeyboardImplX11.class);

	/**
	 * keyboard events stored as the bits of a 'long'. Keycodes are LWJGL keycodes
	 * in case of special characters. 1 in case of unicode characters.
	 * <pre>
	 * |unicode charchode 32 bit        |X11 state 16 bit| keycode 16 bit |
	 * |................................|........s...ac.s|................|
	 * the 24th LSB is the special indicator bit ↑
	 *                                        is alt ↑
	 *                                        is ctrl ↑
	 *                                         is shift ↑
	 * </pre>
	 */
	private long[] events=new long[256];
	private int readPtr;
	private int writePtr;
	
	@Override
	public boolean next() {
		if(readPtr!=writePtr)
		{
			readPtr++;
			readPtr%=events.length;
			return true;
		}
		return false;
	}

	private long getEvent()
	{
		int p=(readPtr+events.length-1)%events.length;
		return events[p];
	}

	@Override
	public int getEventKey() {
		long e=getEvent();
		return ((int)e)&0xFFFF;
	}

	@Override
	public boolean isKeyDown() {
		return true;
	}
	final boolean isSpecialKey(long ev)
	{
		return (ev&1<<24)!=0;
	}
	
	@Override
	public char getEventCharacter() {
		long ev= getEvent();
		if(isSpecialKey(ev))
		{
			return 0;
		}
		char ret=(char) (ev>>32);
		return ret;
	}
	
	private void addEvent(long event)
	{
		events[writePtr]=event;
		writePtr++;
		writePtr%=events.length;
	}

	@Override
	public boolean isSpecialKey() {
		long ev=getEvent();
		return isSpecialKey(ev);
	}

	@Override
	public boolean isCtrl() {
		long ev=getEvent();
		return isCtrl((int)(ev>>16));
	}

	protected boolean isCtrl(int state) {
		return (state&4)!=0;
	}

	@Override
	public boolean isShift() {
		long ev=getEvent();
		return isShift((int)(ev>>16));
	}

	protected boolean isShift(int state) {
		return (state&1)!=0;
	}

	@Override
	public boolean isAlt() {
		long ev=getEvent();
		return isAlt((int)(ev>>16));
	}
	protected boolean isAlt(int state) {
		return (state&8)!=0;
	}
	private class KeyCodeMapping
	{
		public int keycode;
		public int lwjglCode;
		public String name;
		public KeyCodeMapping(int keycode, int lwjglCode, String name) {
			super();
			this.keycode = keycode;
			this.lwjglCode = lwjglCode;
			this.name = name;
		}
	}
	private KeyCodeMapping[] mappings=new KeyCodeMapping[]{};
	private void updateKeycodeMappings()
	{
		mappings=new KeyCodeMapping[]{
			new KeyCodeMapping(9, Keyboard.KEY_ESCAPE, "ESC"),
			new KeyCodeMapping(23, Keyboard.KEY_TAB, "TAB"),
			
			new KeyCodeMapping(67, Keyboard.KEY_F1, "F1"),
			new KeyCodeMapping(68, Keyboard.KEY_F2, "F2"),
			new KeyCodeMapping(69, Keyboard.KEY_F3, "F3"),
			new KeyCodeMapping(70, Keyboard.KEY_F4, "F4"),
			new KeyCodeMapping(71, Keyboard.KEY_F5, "F5"),
			new KeyCodeMapping(72, Keyboard.KEY_F6, "F6"),
			new KeyCodeMapping(73, Keyboard.KEY_F7, "F7"),
			new KeyCodeMapping(74, Keyboard.KEY_F8, "F8"),
			new KeyCodeMapping(75, Keyboard.KEY_F9, "F9"),
			new KeyCodeMapping(76, Keyboard.KEY_F11, "F10"),
			new KeyCodeMapping(95, Keyboard.KEY_F11, "F11"),
			new KeyCodeMapping(96, Keyboard.KEY_F12, "F12"),

			new KeyCodeMapping(113, Keyboard.KEY_LEFT, "Left"),
			new KeyCodeMapping(111, Keyboard.KEY_UP, "Up"),
			new KeyCodeMapping(114, Keyboard.KEY_RIGHT, "Right"),
			new KeyCodeMapping(116, Keyboard.KEY_DOWN, "Down"),
		};
	}

	public void addEvent(int press, int x, int y, int keyCode, int state, int unicode) {
		updateKeycodeMappings();
		if(press==1)
		{
			boolean specialKey=unicode==-1;
			KeyCodeMapping m=null;
			if(specialKey)
			{
				m=decodeKeyCode(keyCode);
			}
			if(OGlGlobalParameters.logKeyMessages && LOG.isDebugEnabled())
			{
				char ch=(char)unicode;
				LOG.debug("kev: p: "+press+" keycode: "+keyCode+" st: "+state+" "
						+decodeState(state)+" "+
						getMapping(unicode, specialKey, m, ch));
			}
			long lwjglKeyCode;
			if(specialKey)
			{
				if(m==null)
				{
					return;
				}
				lwjglKeyCode=m.lwjglCode;
			}else
			{
				// keycode must not be zero!
				lwjglKeyCode=1;
			}
			long ev=(state<<16)|((0xFFFFl&unicode)<<32)|((specialKey?1l:0l)<<24)|lwjglKeyCode;
			addEvent(ev);
		}
	}

	private String getMapping(int unicode, boolean specialKey, KeyCodeMapping m,
			char ch) {
		String retval;
		if(specialKey){
			retval = "mapping: ";
			if(m==null){
				retval+="null";
			}else{
				retval+=m.name;	
			}
				
		}else{
			retval = ""+unicode+" '"+ch+"'";
		}
		return retval;
	}

	private String decodeState(int state) {
		return (isCtrl(state)?"ctrl ":"")+(isShift(state)?"shift ":"")+
				(isAlt(state)?"alt":"");
	}

	private KeyCodeMapping decodeKeyCode(int keyCode) {
		for(KeyCodeMapping m: mappings)
		{
			if(m.keycode==keyCode)
			{
				return m;
			}
		}
		return null;
	}
}
