package hu.qgears.textrender;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.NativeImageEditor;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.RGBAColor;
import hu.qgears.images.text.TextParameters;
import hu.qgears.textrender.stbtt.StbNativeAccessor;

public class TrueTypeRenderer {
	
	private TrueTypeNativeInterface rendererNative;
	private static final RGBAColor TRANSPARENT = new RGBAColor(0,0,0,0);
	public TrueTypeRenderer(boolean stbMode) {
	
		if (stbMode) {
			rendererNative = StbNativeAccessor.getInstance();
		} else {
			throw new RuntimeException("Other lib not impelmented yet");
		}
		
	}
	
	public NativeImage createNativeImageColor(int w, int h) {
		NativeImage ret = NativeImage.create(new SizeInt(w, h), ENativeImageComponentOrder.RGBA, 4,
				DefaultJavaNativeMemoryAllocator.getInstance());
		return ret;
	}
	
	public SizeInt layoutText(TextParameters params, SizeInt desiredBox) {
		return rendererNative.layoutText(
				params.fontFamily,
				params.text,
				params.hAlign,
				params.vAlign,
				desiredBox.getWidth() ,desiredBox.getHeight()
				,params.wrapMode);
	}

	public SizeInt renderText(NativeImage image, TextParameters params, boolean clear) {
		
		long s = rendererNative.createSurfaceWithData(image.getBuffer().getJavaAccessor(), image.getWidth(), image.getHeight());
		if (clear) {
			//TODO should we do it in native code? (for performance reason, memset vs ByteBuffer manipulation from java)
			new NativeImageEditor(image).fillWithColor(TRANSPARENT);
		}
		try {
			float[] c = params.c.toFloatVector();
			
			return rendererNative.renderText(
					s, 
					params.fontFamily,
					params.text,
					params.hAlign,
					params.vAlign,
					0,0,image.getWidth(),image.getHeight(),
					c[0],c[1],c[2],c[3],
					true
					,params.wrapMode);
		} finally {
			rendererNative.disposeSurface(s);
		}
	}
	
}
