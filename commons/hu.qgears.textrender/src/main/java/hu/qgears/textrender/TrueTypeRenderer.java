package hu.qgears.textrender;

import java.util.Objects;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageAlphaStorageFormat;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.TextParameters;
import hu.qgears.textrender.libschrift.LibschriftAccessor;

public class TrueTypeRenderer {
	
	public static final ENativeImageComponentOrder DEFAULT_CO = ENativeImageComponentOrder.BGRA;
	private TrueTypeNativeInterface rendererNative;
	
	private IFontManager fontManager;
	
	public TrueTypeRenderer(TrueTypeNativeInterface rendererNative) {
		this.rendererNative = rendererNative;
	}
	public TrueTypeRenderer() {
		this(LibschriftAccessor.getInstance());
	}
	
	public static NativeImage createNativeImageColor(int w, int h) {
		NativeImage ret = NativeImage.create(new SizeInt(w, h), DEFAULT_CO, 4,
				DefaultJavaNativeMemoryAllocator.getInstance());
		ret.setAlphaStorageFormat(ENativeImageAlphaStorageFormat.premultiplied);
		return ret;
	}
	
	private void validateParams(TextParameters params) {
		Objects.requireNonNull(params, "params");
		Objects.requireNonNull(params.text, "params.text");
		Objects.requireNonNull(params.hAlign, "params.hAlign");
		Objects.requireNonNull(params.vAlign, "params.vAlign");
		Objects.requireNonNull(params.wrapMode, "params.wrapMode");
		if (fontManager == null) {
			throw new IllegalStateException("fontManager is not set");
		}
	}
	
	public SizeInt layoutText(TextParameters params, SizeInt desiredBox) {
		validateParams(params);
		Objects.requireNonNull(desiredBox, "desiredBox");
		try {
			return rendererNative.layoutText(
					fontManager.getFont(params),
					params.text,
					params.hAlign,
					params.vAlign,
					desiredBox.getWidth() ,desiredBox.getHeight()
					,params.wrapMode);
		} catch (Exception e) {
			String rect = String.format(", desiredBox=%dx%d", desiredBox.getWidth(), desiredBox.getHeight());
			throw new RuntimeException("Layout failed on "+params + rect , e);
		}
	}

	public SizeInt renderText(NativeImage image, TextParameters params, boolean clear) {
		return renderText(0,0,image.getWidth(),image.getHeight(), image, params, clear);
	}
	
	public SizeInt renderText(int x, int y, int w, int h, NativeImage image, TextParameters params, boolean clear) {
		Objects.requireNonNull(image, "image");
		validateParams(params);
		Objects.requireNonNull(params.c, "params.c");
		
		long s = rendererNative.createSurfaceWithData(image.getBuffer().getJavaAccessor(), image.getWidth(), image.getHeight(), image.getComponentOrder());
		if (clear) {
			rendererNative.clearSurface(s);
		}
		try {
			float[] c = params.c.toFloatVector();
			
			return rendererNative.renderText(
					s, 
					fontManager.getFont(params),
					params.text,
					params.hAlign,
					params.vAlign,
					x,y,w,h,
					c[0],c[1],c[2],c[3],
					true
					,params.wrapMode);
		
		} catch (Exception e) {
			String rect = String.format(", x=%d, y=%d, w=%d, h=%d, image=%dx%d", x, y, w, h,
					image.getWidth(), image.getHeight());
			throw new RuntimeException("Render failed on "+params + rect, e);
		} finally {
			rendererNative.disposeSurface(s);
		}
	}
	
	public void setFontManager(IFontManager fontManager) {
		this.fontManager = fontManager;
	}
	
	public IFontManager getFontManager() {
		return fontManager;
	}
}
