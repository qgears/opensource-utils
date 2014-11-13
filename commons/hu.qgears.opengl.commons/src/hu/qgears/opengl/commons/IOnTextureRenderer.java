package hu.qgears.opengl.commons;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;



/**
 * Callback interface to render a scene
 * on the openGL context.
 * @author rizsi
 *
 */
public interface IOnTextureRenderer {
	void render(RGlContext glContext, SizeInt size);
}
