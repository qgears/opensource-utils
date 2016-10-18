package hu.qgears.images.devil;

import java.nio.ByteBuffer;

public class NativeDevILConnector
{
	protected long ptr;
	protected native void initDevIL();
	protected native int bindImage();
	protected native int getTypeId(String ext);
	protected native int loadImage(ByteBuffer content, int typeId);
	protected native ByteBuffer convertImage();
	protected native int getWidthPrivate();
	protected native int getHeightPrivate();
	protected native void init();
	protected native void saveImage(ByteBuffer buffer, String outFile, int width,
			int height);
	protected native void nativeDispose();
}
