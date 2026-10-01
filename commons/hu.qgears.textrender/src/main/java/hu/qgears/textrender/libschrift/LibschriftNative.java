package hu.qgears.textrender.libschrift;
import java.nio.ByteBuffer;

import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.EHorizontalAlign;
import hu.qgears.images.text.EVerticalAlign;
import hu.qgears.images.text.EWrapMode;
import hu.qgears.textrender.TrueTypeFont;
import hu.qgears.textrender.TrueTypeNativeInterface;

/*package*/ class LibschriftNative implements TrueTypeNativeInterface {

	@Override
	public long createSurfaceWithData(ByteBuffer data, int w, int h, ENativeImageComponentOrder co) {
		if (data == null) {
			throw new NullPointerException("data");
		}
		int rowStride = w;
		switch (co) {
		case BGRA:
			break;
		case ALPHA:
			rowStride = ((w +3 ) & ~3);
			break;
		default:
			throw new RuntimeException("Unsupported color format " + co);
		}
		if (data.capacity() < rowStride * h * co.getNCHannels()) {

			throw new IllegalArgumentException("invalid buffer size");
		}
		return createSurfaceWithDataPrivate(data, w, h, co.ordinal());
	}

	private native long createSurfaceWithDataPrivate(ByteBuffer data, int w, int h, int pixelFormat);

	@Override
	public SizeInt renderText(long surfaceHandle, TrueTypeFont font, String text, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int x, int y, int width, int height, float r, float g, float b, float a,
			boolean clip, EWrapMode wrapMode) {
		if (surfaceHandle == 0) {
			throw new IllegalArgumentException("surfaceHandle must not be 0");
		}
		if (font == null) {
			throw new IllegalArgumentException("font must not be null");
		}
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		if (hAlign == null) {
			throw new IllegalArgumentException("hAlign must not be null");
		}
		if (vAlign == null) {
			throw new IllegalArgumentException("vAlign must not be null");
		}
		if (width < 0) {
			throw new IllegalArgumentException("width must not be negative: " + width);
		}
		if (height < 0) {
			throw new IllegalArgumentException("height must not be negative: " + height);
		}
		if (wrapMode == null) {
			throw new IllegalArgumentException("wrapMode must not be null");
		}
		if (r < 0f || r > 1f) {
			throw new IllegalArgumentException("r must be in range [0,1]: " + r);
		}
		if (g < 0f || g > 1f) {
			throw new IllegalArgumentException("g must be in range [0,1]: " + g);
		}
		if (b < 0f || b > 1f) {
			throw new IllegalArgumentException("b must be in range [0,1]: " + b);
		}
		if (a < 0f || a > 1f) {
			throw new IllegalArgumentException("a must be in range [0,1]: " + a);
		}
		return renderTextPrivate(surfaceHandle, font, text, hAlign, vAlign, x, y, width, height, r, g, b, a, clip,
				wrapMode);
	}

	private native SizeInt renderTextPrivate(long surfaceHandle, TrueTypeFont font, String text, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int x, int y, int width, int height, float r, float g, float b, float a,
			boolean clip, EWrapMode wrapMode);

	@Override
	public SizeInt layoutText(TrueTypeFont font, String text, EHorizontalAlign hAlign, EVerticalAlign vAlign, int width,
			int height, EWrapMode wrapMode) {
		if (font == null) {
			throw new IllegalArgumentException("font must not be null");
		}
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		if (hAlign == null) {
			throw new IllegalArgumentException("hAlign must not be null");
		}
		if (vAlign == null) {
			throw new IllegalArgumentException("vAlign must not be null");
		}
		if (width < 0) {
			throw new IllegalArgumentException("width must not be negative: " + width);
		}
		if (height < 0) {
			throw new IllegalArgumentException("height must not be negative: " + height);
		}
		if (wrapMode == null) {
			throw new IllegalArgumentException("wrapMode must not be null");
		}
		return layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode);
	}

	private native SizeInt layoutTextPrivate(TrueTypeFont font, String text, EHorizontalAlign hAlign,
			EVerticalAlign vAlign, int width, int height, EWrapMode wrapMode);

	@Override
	public void disposeSurface(long surfaceHandle) {
		if (surfaceHandle == 0) {
			throw new IllegalArgumentException("surfaceHandle must not be 0");
		}
		disposeSurfacePrivate(surfaceHandle);
	}
	private native void disposeSurfacePrivate(long surfaceHandle);

	@Override
	public void clearSurface(long surfaceHandle) {
		if (surfaceHandle == 0) {
			throw new IllegalArgumentException("surfaceHandle must not be 0");
		}
		clearSurfacePrivate(surfaceHandle);
	}

	private native void clearSurfacePrivate(long surfaceHandle);
	
}
