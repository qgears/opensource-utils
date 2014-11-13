package hu.qgears.opengl.commons;

import hu.qgears.commons.IDisposeable;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;

public interface IRenderOnTexture extends IDisposeable {

	void render(RGlContext rGlContext, IOnTextureRenderer iOnTextureRenderer);

	/**
	 * Render scene on a texture.
	 * @param glContext openGL context that stores the current state of oepnGL
	 * @param renderMethod callback that does the rendering
	 * @param out copy the resulting texture into this native image (can be null)
	 */
	void render(RGlContext glContext, IOnTextureRenderer renderMethod, NativeImage out);

	/**
	 * Use the on buffer rendering in a procedural - not embedded way. 
	 */
	void beginRender(RGlContext glContext);
	/**
	 * Use the on buffer rendering in a procedural - not embedded way. 
	 * @param out rendered image is copied into this native image. May be null - no copy is done.
	 */
	void endRender(RGlContext glContext, NativeImage out);
	
	Texture getTarget();
	
	SizeInt getSize();
}
