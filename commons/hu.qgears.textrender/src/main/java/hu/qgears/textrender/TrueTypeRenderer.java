package hu.qgears.textrender;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageAlphaStorageFormat;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.TextParameters;
import hu.qgears.textrender.libschrift.LibschriftAccessor;
import hu.qgears.textrender.stbtt.StbNativeAccessor;

public class TrueTypeRenderer {
	
	public static final ENativeImageComponentOrder DEFAULT_CO = ENativeImageComponentOrder.BGRA;
	private TrueTypeNativeInterface rendererNative;
	
	private IFontManager fontManager= new DefaultFontManger();
	
	public TrueTypeRenderer(TrueTypeNativeInterface rendererNative) {
		this.rendererNative = rendererNative;
	}
	public TrueTypeRenderer(boolean stbMode) {
		if (stbMode) {
			rendererNative = StbNativeAccessor.getInstance();
		} else {
			rendererNative = LibschriftAccessor.getInstance();
		}
	}
	
	public static NativeImage createNativeImageColor(int w, int h) {
		NativeImage ret = NativeImage.create(new SizeInt(w, h), DEFAULT_CO, 4,
				DefaultJavaNativeMemoryAllocator.getInstance());
		ret.setAlphaStorageFormat(ENativeImageAlphaStorageFormat.premultiplied);
		return ret;
	}
	
	public SizeInt layoutText(TextParameters params, SizeInt desiredBox) {
		return rendererNative.layoutText(
				fontManager.getFont(params),
				params.text,
				params.hAlign,
				params.vAlign,
				desiredBox.getWidth() ,desiredBox.getHeight()
				,params.wrapMode);
	}

	public SizeInt renderText(NativeImage image, TextParameters params, boolean clear) {
		return renderText(0,0,image.getWidth(),image.getHeight(), image, params, clear);
	}
	
	public SizeInt renderText(int x, int y, int w, int h, NativeImage image, TextParameters params, boolean clear) {
		
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
