package hu.qgears.opengl.lwjgl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import hu.qgears.opengl.commons.input.IKeyboard;

public class KeyboardImplLwjgl implements IKeyboard, GLFWKeyCallbackI {

	private class Event {
		int key;
		int scancode;
		int action;
		int mods;
	}
	

	private Event[] events;
	private int readPtr;
	private int writePtr;

	public KeyboardImplLwjgl() {
		events=new Event[256];
		for (int i = 0; i < events.length;i++) {
			events[i]= new Event();
		}
	}
	
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

	private Event getEvent()
	{
		int p=(readPtr+events.length-1)%events.length;
		return events[p];
	}

	@Override
	public int getEventKey() {
		return getEvent().key;
	}

	@Override
	public boolean isKeyDown() {
		Event e = getEvent();
		return e.action == GLFW.GLFW_PRESS;
	}

	@Override
	public boolean isSpecialKey() {
		int k = getEventKey();
		return k >= GLFW.GLFW_KEY_F1;
	}

	@Override
	public char getEventCharacter() {
		int k = getEventKey();
		if (k >= GLFW.GLFW_KEY_F1) {
			return 0;
		}
		/*
		 * TODO Very lame impl
		 * GLFWCharCallbackI should be used for Unicode character support if necessary
		 */
		return (char)k;
	}

	@Override
	public boolean isCtrl() {
		return (getEvent().mods & GLFW.GLFW_MOD_CONTROL) != 0;
	}

	@Override
	public boolean isShift() {
		return (getEvent().mods & GLFW.GLFW_MOD_SHIFT) != 0;
	}

	@Override
	public boolean isAlt() {
		return (getEvent().mods & GLFW.GLFW_MOD_ALT) != 0;
	}

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		//callback from GLFW
		events[writePtr].key = key;
		events[writePtr].scancode = scancode;
		events[writePtr].action = action;
		events[writePtr].mods = mods;
		writePtr++;
		writePtr%=events.length;
	}

}
