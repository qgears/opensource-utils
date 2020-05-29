package hu.qgears.opengl.osmesa;

import java.nio.ByteBuffer;

public class OSMesaNative {
	protected native void createContext();
	static protected native void execPreload();
	protected native void makeCurrentPrivate(ByteBuffer image, int width, int height);
	protected native void disposeContext();
}
