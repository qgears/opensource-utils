package hu.qgears.opengl.lwjgl;

import hu.qgears.opengl.commons.input.IKeyboard;

import org.lwjgl.input.Keyboard;

public class KeyboardImplLwjgl implements IKeyboard {

	@Override
	public boolean next() {
		return Keyboard.next();
	}

	@Override
	public int getEventKey() {
		return Keyboard.getEventKey();
	}

	@Override
	public boolean isKeyDown() {
		return Keyboard.isKeyDown(getEventKey());
	}

	@Override
	public char getEventCharacter() {
		return Keyboard.getEventCharacter();
	}

	@Override
	public boolean isSpecialKey() {
		int key=Keyboard.getEventKey();
		return key>=Keyboard.KEY_F1&&key<=Keyboard.KEY_F15;
	}

	@Override
	public boolean isCtrl() {
		return false;
	}

	@Override
	public boolean isShift() {
		return false;
	}

	@Override
	public boolean isAlt() {
		return false;
	}

}
