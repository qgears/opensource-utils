package hu.qgears.opengl.glut;

import hu.qgears.opengl.commons.input.IKeyboard;

import org.lwjgl.input.Keyboard;

public class KeyboardImplGlut implements IKeyboard {
	/**
	 * keyboard events stored as the bits of a 'long'
	 * <pre>
	 * |charchode 32 bit                | state 16 bit   | keycode 16 bit |
	 * |................................|........s....acs|................|
	 * the 24th LSB is the special indicator bit ↑
	 *                                         is alt ↑
	 *                                         is ctrl ↑
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
	int[] fs=new int[]{Keyboard.KEY_F1,
			Keyboard.KEY_F2,
			Keyboard.KEY_F3,
			Keyboard.KEY_F4,
			Keyboard.KEY_F5,
			Keyboard.KEY_F6,
			Keyboard.KEY_F7,
			Keyboard.KEY_F8,
			Keyboard.KEY_F9,
			Keyboard.KEY_F10,
			Keyboard.KEY_F11,
			Keyboard.KEY_F12,
	};

	int[] arrows = new int[]{
		Keyboard.KEY_LEFT,
		Keyboard.KEY_UP,
		Keyboard.KEY_RIGHT,
		Keyboard.KEY_DOWN,
		Keyboard.KEY_P
	};
	
	
	@Override
	public int getEventKey() {
		long e=getEvent();
		return transformKeycodeToLwjglKeyCode(e);
	}

	/**
	 * Converts keycodes comes from GLUT to LWJGL specific keycodes. The NativeClient handles the LWJGL keyCodes,
	 * so this conversion is necessary
	 * 
	 * <p>
	 * TODO the native client also transforms the keycodes before they will be sent to Java server. It will be great
	 * to simplify this. See NativeClient#translateKeyCodeToBrowserKeyCode()
	 * @param e
	 * @return
	 */
	private int transformKeycodeToLwjglKeyCode(long  e) {
		char c=(char) e;
		if(isSpecialKey(e))
		{
			//handling function keys
			int index=c-1;
			if(index>=0 &&index< fs.length)
			{
				return fs[index];
			}
			//handling arrow keys
			index = c - 'd';
			if (index >=0 && index < arrows.length){
				return arrows[index];
			}
		}
		//not special key but has no charcode
		if (getEventCharacter() == 0){
			if(c==27)
			{
				return Keyboard.KEY_ESCAPE;
			}
			if (c == 9){
				return Keyboard.KEY_TAB;
			}
		}
		return (int)e;
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
//		System.out.println();
//		System.out.println("Ctrl " + isCtrl());
//		System.out.println("Alt " + isAlt());
//		System.out.println("Shift " + isShift());
//		System.out.println("Char "+(char)ev+ "("+(int)((char)ev) + ")");
		if(isSpecialKey(ev))
		{
			return 0;
		}
		char ret=(char) (ev>>32);
		return ret;
	}
	
	public void addEvent(long event)
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
		return ((ev>>16)&2)!=0;
	}

	@Override
	public boolean isShift() {
		long ev=getEvent();
		return ((ev>>16)&1)!=0;
	}

	@Override
	public boolean isAlt() {
		long ev=getEvent();
		return ((ev>>16)&4)!=0;
	}
	
}
