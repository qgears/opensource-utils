package hu.qgears.textrender.stbtt;
import java.nio.ByteBuffer;

import hu.qgears.images.SizeInt;
import hu.qgears.images.text.EHorizontalAlign;
import hu.qgears.images.text.EVerticalAlign;
import hu.qgears.images.text.EWrapMode;
import hu.qgears.textrender.TrueTypeNativeInterface;

/*package*/ class StbTrueTypeNative implements TrueTypeNativeInterface {

	@Override
	public int createSurfaceWithData(ByteBuffer data, int w, int h) {
		if (data == null) {
			throw new NullPointerException("data");
		}
		if (data.capacity() < w * h * 4) {

			throw new IllegalArgumentException("invalid buffer size");
		}
		return createSurfaceWithDataPrivate(data, w, h);
	}

	private native int createSurfaceWithDataPrivate(ByteBuffer data, int w, int h);

	@Override
	public SizeInt renderText(int surfaceHandle, String fontFamily, String str, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int x, int y, int width, int height, float r, float g, float b, float a,
			boolean clip, EWrapMode wrapMode) {

		// TODO parameter verficifation : handle null args here instead of the native
		// impl
		return renderTextPrivate(surfaceHandle, fontFamily, str, hAlign, vAlign, x, y, width, height, r, g, b, a, clip,
				wrapMode);
	}

	private native SizeInt renderTextPrivate(int surfaceHandle, String fontFamily, String str, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int x, int y, int width, int height, float r, float g, float b, float a,
			boolean clip, EWrapMode wrapMode);

	@Override
	public SizeInt layoutText(String fontFamily, String text, EHorizontalAlign hAlign, EVerticalAlign vAlign, int width,
			int height, EWrapMode wrapMode) {
		// TODO parameter verification
		return layoutTextPrivate(fontFamily, text, hAlign, vAlign, width, height, wrapMode);
	}

	private native SizeInt layoutTextPrivate(String fontFamily, String text, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int width, int height, EWrapMode wrapMode);

	
}
