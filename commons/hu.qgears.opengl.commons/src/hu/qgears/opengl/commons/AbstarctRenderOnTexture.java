package hu.qgears.opengl.commons;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;

abstract public class AbstarctRenderOnTexture implements IRenderOnTexture {
	IRenderOnTexture prev;
	protected SizeInt size;
	@Override
	final public SizeInt getSize() {
		return size;
	}

	@Override
	final public void beginRender(RGlContext ctx) {
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
	final public void render(RGlContext glContext, IOnTextureRenderer renderMethod, NativeImage out)
	{
		beginRender(glContext);
		renderMethod.render(glContext, size);
		endRender(glContext, out);
	}
	
	@Override
	final public void endRender(RGlContext ctx, NativeImage out) {
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


	abstract protected void unbind();

	abstract protected void stealImageData(RGlContext ctx, NativeImage out);

	abstract protected void bind(boolean b);
}
