package hu.qgears.opengl.commons;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;

public abstract class AbstarctRenderOnTexture implements IRenderOnTexture {
	IRenderOnTexture prev;
	protected SizeInt size;
	@Override
	public final SizeInt getSize() {
		return size;
	}

	@Override
	public final void beginRender(RGlContext ctx) {
		prev=ctx.getCurrentRenderOnTexture();
		bind(false);
		ctx.setCurrentRenderOnTexture(this);
	}
	/**
	 * Render scene on a texture.
	 * @param glContext openGL context that stores the current state of oepnGL
	 * @param renderMethod callback that does the rendering
	 * @param out copy the resulting texture into this native image (can be null)
	 */
	public final void render(RGlContext glContext, IOnTextureRenderer renderMethod, NativeImage out)
	{
		beginRender(glContext);
		renderMethod.render(glContext, size);
		endRender(glContext, out);
	}
	
	@Override
	public final void endRender(RGlContext ctx, NativeImage out) {
		if(out!=null)
		{
			stealImageData(ctx, out);
		}
		unbind();
		if(prev!=null)
		{
			((AbstarctRenderOnTexture)prev).bind(false);
		}
		ctx.setCurrentRenderOnTexture(prev);
	}


	protected abstract void unbind();

	protected abstract void stealImageData(RGlContext ctx, NativeImage out);

	protected abstract void bind(boolean b);
}
