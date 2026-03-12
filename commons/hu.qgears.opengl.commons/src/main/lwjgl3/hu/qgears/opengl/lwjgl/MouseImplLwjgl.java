package hu.qgears.opengl.lwjgl;

import org.apache.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.GlMouseEvent;
import hu.qgears.opengl.commons.input.IMouse;

public class MouseImplLwjgl implements IMouse {
	private static final Logger LOG = Logger.getLogger(MouseImplLwjgl.class);
	private GLContextProviderLwjgl provider;

	private GlMouseEvent[] events;
	private int readPtr;
	private int writePtr;
	private int x;
	private int y;

	public MouseImplLwjgl(GLContextProviderLwjgl provider) {
		this.provider = provider;
		events = new GlMouseEvent[256];
		for (int i = 0; i < events.length; i++) {
			events[i] = new GlMouseEvent();
		}
	}

	private boolean next() {
		if (readPtr != writePtr) {
			readPtr++;
			readPtr %= events.length;
			return true;
		}
		return false;
	}

	private GlMouseEvent getCurrent() {
		int p = (readPtr + events.length - 1) % events.length;
		return events[p];
	}

	@Override
	public void poll() {
	}

	@Override
	public boolean isButtonDown(EMouseButton b) {
		int button = -1;
		switch (b) {
		case LEFT:
			button = GLFW.GLFW_MOUSE_BUTTON_LEFT;
			break;
		case MIDDLE:
			button = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
			break;
		case RIGHT:
			button = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
			break;
		case WHEEL_DOWN:
		case WHEEL_UP:
		default:
			break;
		}
		if (button == -1) {
			return false;
		} else {
			int state = GLFW.glfwGetMouseButton(provider.window, button);
			return state == GLFW.GLFW_PRESS;
		}
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public GlMouseEvent getNextEvent() {
		if (next()) {
			GlMouseEvent currentEvent = getCurrent();
			return currentEvent;
		}
		return null;
	}

	/*
	 * Not supported in this impl 
	 */
	@Override
	public void addEvent(final int type, final int x, final int y, final EMouseButton button, final int state) {
	}

	private void addEvent(int x, int y, EMouseButton button, boolean state) {
		// XXX This is not thread safe but we should call it from single thread
		int p = writePtr++;
		writePtr %= events.length;

		events[p].x = x;
		events[p].y = y;
		events[p].button = button;
		events[p].buttonState = state;
		events[p].nanoseconds = System.nanoTime();
	}

	/**
	 * 
	 * Implements {@link GLFWScrollCallbackI}.
	 * 
	 * @param window
	 * @param xoffset
	 * @param yoffset
	 */
	public void scrollEvent(long window, double xoffset, double yoffset) {
		// callback from GLFW
		addEvent(x, y, yoffset < 0? EMouseButton.WHEEL_DOWN : EMouseButton.WHEEL_UP, false);
	}

	/**
	 * Implements {@link GLFWCursorPosCallbackI}
	 * 
	 * @param window
	 * @param xPos
	 * @param yPos
	 */
	public void cursorEvent(long window, double xPos, double yPos) {
		// callback from GLFW
		this.x = (int) xPos;
		this.y = (int) xPos;
	}

	/**
	 * Implements {@link GLFWMouseButtonCallbackI}
	 * 
	 * @param window
	 * @param button
	 * @param action
	 * @param mods
	 */
	public void mouseButtonEvent(long window, int button, int action, int mods) {
		// callback from GLFW
		EMouseButton b = null;
		switch (button) {
		case GLFW.GLFW_MOUSE_BUTTON_LEFT:
			b = EMouseButton.LEFT;
			break;
		case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
			b = EMouseButton.RIGHT;
			break;
		case GLFW.GLFW_MOUSE_BUTTON_MIDDLE:
			b = EMouseButton.MIDDLE;
			break;
		default:
			break;
		}
		if (b != null) {
			addEvent(x, y, b, action == GLFW.GLFW_PRESS);
		}
	}
}
