package hu.qgears.textrender.libschrift;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

import hu.qgears.images.SizeInt;
import hu.qgears.images.text.EHorizontalAlign;
import hu.qgears.images.text.EVerticalAlign;
import hu.qgears.images.text.EWrapMode;
import hu.qgears.textrender.TrueTypeNativeInterface;

/*package*/ class LibschriftNative implements TrueTypeNativeInterface {

	@Override
	public long createSurfaceWithData(ByteBuffer data, int w, int h) {
		if (data == null) {
			throw new NullPointerException("data");
		}
		if (data.capacity() < w * h * 4) {

			throw new IllegalArgumentException("invalid buffer size");
		}
		return createSurfaceWithDataPrivate(data, w, h);
	}

	private native long createSurfaceWithDataPrivate(ByteBuffer data, int w, int h);

	@Override
	public SizeInt renderText(long surfaceHandle, String fontFamily, String str, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int x, int y, int width, int height, float r, float g, float b, float a,
			boolean clip, EWrapMode wrapMode) {

		// TODO parameter verficifation : handle null args here instead of the native
		// impl
		return renderTextPrivate(surfaceHandle, fontFamily, str, hAlign, vAlign, x, y, width, height, r, g, b, a, clip,
				wrapMode);
	}

	private native SizeInt renderTextPrivate(long surfaceHandle, String fontFamily, String str, EHorizontalAlign hAlign,
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

	@Override
	public void disposeSurface(long surfaceHandle) {
		disposeSurfacePrivate(surfaceHandle);
	}
	private native void disposeSurfacePrivate(long surfaceHandle);

	
}
