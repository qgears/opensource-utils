package hu.qgears.opengl.libinput;

import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.input.Keyboard;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.opengl.commons.input.IKeyboard;

public class LibinputKeyboard implements IKeyboard
{
	private long[] events=new long[256];
	private boolean shift, ctrl, alt;
	private int readPtr;
	private int writePtr;
	private Map<Integer, Character> characters=new TreeMap<Integer, Character>();
	private Map<Integer, Integer> specials=new TreeMap<Integer, Integer>();
	/**
	 * Currently processed event "key" value - as in libinput API.
	 */
	private int key;
	/**
	 * Currently processed event "keystate" value - as in libinput API.
	 */
	private int keystate;
	
	/**
	 * TODO Simple layout of raw scan codes to characters.
	 */
	int[] layout=new int[] {
			16, 'q',
			17, 'w',
			18, 'e',
			19, 'r',
			20, 't',
			21, 'y',
			22, 'u',
			23, 'i',
			24, 'o',
			25, 'p',
			26, '[',
			27, ']',
			
			30, 'a',
			31, 's',
			32, 'd',
			33, 'f',
			34, 'g',
			35, 'h',
			36, 'j',
			37, 'k',
			38, 'l',
			39, ';',

			44, 'z',
			45, 'x',
			46, 'c',
			47, 'v',
			48, 'b',
			49, 'n',
			50, 'm',
			51, ',',
			52, '.',
			53, '/',
			57, ' ',
	};
	
	/**
	 * TODO minimal layout of 'special' non character keys.
	 * Most of the codes seem to be equal to the LWJGL codes. Exception: arrow keys
	 */
	int[] layoutSpecials=
		{
			1, Keyboard.KEY_ESCAPE,
			14, Keyboard.KEY_BACK,
			15, Keyboard.KEY_TAB,
			28, Keyboard.KEY_RETURN,
			59, Keyboard.KEY_F1,
			60, Keyboard.KEY_F2,
			61, Keyboard.KEY_F3,
			62, Keyboard.KEY_F4,
			63, Keyboard.KEY_F5,
			64, Keyboard.KEY_F6,
			65, Keyboard.KEY_F7,
			66, Keyboard.KEY_F8,
			67, Keyboard.KEY_F9,
			68, Keyboard.KEY_F10,
			87, Keyboard.KEY_F11,
			88, Keyboard.KEY_F12,
			105, Keyboard.KEY_LEFT,
			106, Keyboard.KEY_RIGHT,
			103, Keyboard.KEY_UP,
			108, Keyboard.KEY_DOWN,
			111, Keyboard.KEY_DELETE,
			42, Keyboard.KEY_LSHIFT,
			56, Keyboard.KEY_LMENU,     // LEFT ALT
			29, Keyboard.KEY_LCONTROL   // LEFT CONTROL
		};

	@Override
	public boolean next() {
		synchronized (this) {
			if(readPtr!=writePtr)
			{
				long currentEvent=events[readPtr];
				key=(int)currentEvent;
				keystate=(int)(currentEvent>>32);
				switch (key) {
				case 42:	// Shift
					shift=keystate==1;
					break;
				case 56:	// Alt
					alt=keystate==1;
					break;
				case 29:
					ctrl=keystate==1;
					break;
				default:
					break;
				}
				readPtr++;
				readPtr%=events.length;
				return true;
			}
			return false;
		}
	}
	private void processKeyboardEvent(LibinputEvent msg) {
		int key=msg.a;
		int keystate=msg.b;
		long event=(((long)keystate)<<32)+((long)key&0xFFFFFFFFl);
		addEvent(event);
	}
	
	private void addEvent(long event)
	{
		synchronized (this) {
			events[writePtr]=event;
			writePtr++;
			writePtr%=events.length;
		}
	}

	@Override
	public int getEventKey() {
		return decodeKey(key);
	}

	private int decodeKey(int scancode) {
		final Integer ret=specials.get(scancode);
		
		if (ret == null) {
			throw new IllegalArgumentException(String.format("Cannot decode "
					+ "special scan code: %d / %08X", scancode));
		} else {
			return ret;
		}
	}
	@Override
	public boolean isKeyDown() {
		return keystate==1;
	}

	@Override
	public boolean isSpecialKey() {
		return !characters.containsKey(key);
	}

	@Override
	public char getEventCharacter() {
		Character ret=characters.get(key);
		return ret==null?0:ret;
	}

	@Override
	public boolean isCtrl() {
		return ctrl;
	}

	@Override
	public boolean isShift() {
		return shift;
	}

	@Override
	public boolean isAlt() {
		return alt;
	}

	/**
	 * Attach this keyboard to the Libinput instance.
	 * Only to be used by the Window context provider.
	 * @param li
	 */
	public void init(Libinput li) {
		for(int i=0;i<layout.length;i+=2)
		{
			characters.put(layout[i], (char)layout[i+1]);
		}
		for(int i=0;i<layoutSpecials.length;i+=2)
		{
			specials.put(layoutSpecials[i], layoutSpecials[i+1]);
		}
		li.keyboard.addListener(new UtilEventListener<LibinputEvent>() {
			@Override
			public void eventHappened(LibinputEvent msg) {
				processKeyboardEvent(msg);
			}
		});
	}
}
