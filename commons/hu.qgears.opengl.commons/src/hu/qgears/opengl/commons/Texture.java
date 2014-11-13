package hu.qgears.opengl.commons;

import hu.qgears.commons.IDisposeable;
import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.images.ENativeImageAlphaStorageFormat;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.EBlendFunc;
import hu.qgears.opengl.commons.context.RGlContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutionException;

import org.lwjgl.opengl.APPLEClientStorage;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


/**
 * An OpenGL texture that can be rendered on OpenGL context.
 * 
 * Can contain pixel data of ENativeImageComponentOrder types.
 * The image can be rendered as a sprite.
 * 
 * All methods must be called on OpenGL context thread.
 *
 * @author rizsi
 *
 */
public class Texture implements IDisposeable {
	private boolean samplingNear;
	private int textureHandle;
	private EBlendFunc blendFunc=EBlendFunc.off;
	private boolean disposed = false;
	private int width, height;
	public NativeImage sourceImage;
	public EMipMapType sourceMipmapType;
	public ETextureWrapType sourceTextureWrapType;

	public EBlendFunc getBlendFunc() {
		return blendFunc;
	}

	public void setBlendFunc(EBlendFunc blendFunc) {
		this.blendFunc = blendFunc;
	}

	public int getTextureHandle() {
		return textureHandle;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public SizeInt getSize()
	{
		return new SizeInt(width, height);
	}

	private Texture(int textureHandle, int w, int h, int format, int border) {
		super();
		this.textureHandle = textureHandle;
		this.width = w;
		this.height = h;
		this.formatInt = format;
		this.border = border;
	}

	public static int defaultAligment = 1;

	public static Texture create(SizeInt size,  EMipMapType mtype) throws IOException,
	InterruptedException, ExecutionException {
		ByteBuffer pixels = UtilGl.allocBytes(size.getNumberOfPixels() * 4);
		NativeImage im=new NativeImage(
				new DefaultJavaNativeMemory(pixels), size, ENativeImageComponentOrder.RGBA, 4);
		return create(im, mtype);
	}

	/**
	 * Create texture object and upload the image to its content.
	 *
	 * @param image
	 * @return
	 */
	public static Texture create(NativeImage image) {
		return create(image, EMipMapType.none);
	}
	public static Texture create(NativeImage rdtc, EMipMapType mtype) {
		return create(rdtc, mtype, ETextureWrapType.mirroredRepeat, false);
	}
	public static Texture create(NativeImage rdtc, EMipMapType mtype, ETextureWrapType wrapType, boolean appleClientStorage) {
		int maxSize = UtilGl.getMaxTextureSize();
		int w = rdtc.getWidth();
		int h = rdtc.getHeight();
		if (w > maxSize || h > maxSize) {
			return null;
		}
		int handle=allocateTexture();
		if (handle > 0) {
			Texture ret=new Texture(handle, w, h, GL11.GL_RGBA8, 0);
			ret.replaceContent(rdtc, mtype, wrapType, appleClientStorage);
			return ret;
		}
		return null;
	}

	public static Texture create(SizeInt size) {
		return create(size.getWidth(), size.getHeight());
	}
	public static Texture create(int w, int h) {
		ByteBuffer pixels = UtilGl.allocBytes(w * h * 4);
		NativeImage image=new NativeImage(
				new DefaultJavaNativeMemory(
		pixels), new SizeInt(w, h), ENativeImageComponentOrder.RGBA, 4);
		int handle=makeTexture(image);
		if (handle > 0) {
			return new Texture(handle, w, h, GL11.GL_RGBA8, 0);
		}
		return null;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			IntBuffer textureHandle = UtilGl.allocInts(1);
			textureHandle.put(this.textureHandle).flip();
			GL11.glDeleteTextures(textureHandle);
			disposed = true;
		} else {
			throw new RuntimeException("Already disposed!");
		}
	}

	private static int makeTexture(NativeImage image) {
		ByteBuffer pixels = image.getBuffer().getJavaAccessor();
		int w = image.getWidth();
		int h = image.getHeight();
		ENativeImageComponentOrder componentOrder = image.getComponentOrder();
		// get a new empty texture
		int textureHandle = allocateTexture();
		// preserve currently bound texture, so glBindTexture() below won't
		// affect anything)
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
		// 'select' the new texture by it's handle
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		// set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR); // GL11.
		// GL_NEAREST
		// );
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR); // GL11.
		// GL_NEAREST
		int openGLtype = getOpenGLType(componentOrder);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, // level of detail
				GL11.GL_RGBA8, // internal format for texture is RGB with
				// Alpha
				// GL13.GL_COMPRESSED_RGBA, // internal format for texture
				// is compressed RGB with Alpha
				w, h, // size of texture image
				0, // no border
				openGLtype, // incoming pixel format: 4 bytes in RGBA
				// order
				GL11.GL_UNSIGNED_BYTE, // incoming pixel data type: unsigned
				// bytes
				pixels); // incoming pixels

		// restore previous texture settings
		GL11.glPopAttrib();

		return textureHandle;
	}

	private static int getOpenGLType(ENativeImageComponentOrder componentOrder) {
		switch (componentOrder) {
		case RGB:
			return GL11.GL_RGB;
		case RGBA:
			return GL11.GL_RGBA;

		}
		return GL11.GL_RGB;
	}
	// /**
	// * Create a texture from the given pixels in the default OpenGL RGBA pixel
	// * format. Configure the texture to repeat in both directions and use
	// LINEAR
	// * for magnification.
	// * <P>
	// *
	// * @return the texture handle
	// */
	// public static int makeTexture(
	// ByteBuffer pixels, int w, int h,
	// boolean anisotropic) {
	// // get a new empty texture
	// int textureHandle = allocateTexture();
	// // preserve currently bound texture, so glBindTexture() below won't
	// // affect anything)
	// GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
	// // 'select' the new texture by it's handle
	// GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
	// // set texture parameters
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
	// GL11.GL_REPEAT);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
	// GL11.GL_REPEAT);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
	// GL11.GL_LINEAR); // GL11.GL_NEAREST);
	// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
	// GL11.GL_LINEAR); // GL11.GL_NEAREST);
	//
	// // make texture "anisotropic" so it will minify more gracefully
	// if (anisotropic
	// && UtilGl.getInstance().extensionExists(
	// "GL_EXT_texture_filter_anisotropic")) {
	// // Due to LWJGL buffer check, you can't use smaller sized buffers
	// // (min_size = 16 for glGetFloat()).
	// FloatBuffer max_a = UtilGl.allocFloats(16);
	// // Grab the maximum anisotropic filter.
	// GL11
	// .glGetFloat(
	// EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
	// max_a);
	// // Set up the anisotropic filter.
	// GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
	// EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
	// max_a.get(0));
	// }
	//
	// // Create the texture from pixels
	// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, // level of detail
	// GL11.GL_RGBA8, // internal format for texture is RGB with Alpha
	// w, h, // size of texture image
	// 0, // no border
	// GL11.GL_RGBA, // incoming pixel format: 4 bytes in RGBA order
	// GL11.GL_UNSIGNED_BYTE, // incoming pixel data type: unsigned
	// // bytes
	// pixels); // incoming pixels
	//
	// // restore previous texture settings
	// GL11.glPopAttrib();
	//
	// return textureHandle;
	// }

	/**
	 * Allocate a texture (glGenTextures) and return the handle to it.
	 */
	private static int allocateTexture() {
		return GL11.glGenTextures();
	}

	/**
	 * Draw the texture in ortho mode (2D) over the entire viewport area.
	 * Converts the image to a texture and maps onto a viewport-sized quad.
	 * Depth test is turned off, lighting is off, color is set to white. Alpha
	 * blending is on, so transparent areas will be respected.
	 * <P>
	 * NOTE: By default the viewport is the same size as the window so this
	 * function will draw the image over the entire window. If you setViewport()
	 * to a custom size the image will be drawn into the custom viewport area.
	 * To insure that the image is drawn truly full screen, call resetViewport()
	 * before drawImageFullScreen().
	 * <P>
	 *
	 * @see loadImage(String)
	 * @see setViewport(int,int,int,int)
	 * @see resetViewport()
	 */
	public void drawTextureOnRectangle(TargetRectangle rectangle) {
		drawTextureOnRectangle(rectangle, true);
	}
	private Vector4f color=new Vector4f(1,1,1,1);
	public Vector4f getColor() {
		return color;
	}

	public void setColor(Vector4f color) {
		this.color = color;
	}

	/**
	 * Render the ractangle using its self settings of color and blendfunc.
	 * @param rglContext
	 * @param targetRectangle
	 * @param sourceRectangle
	 */
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle targetRectangle,
			TargetRectangle sourceRectangle) {
		float maxU = sourceRectangle.getBottomRight().getX();
		float maxV = sourceRectangle.getTopLeft().getY();
		float minU = sourceRectangle.getTopLeft().getX();
		float minV = sourceRectangle.getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//		rglContext.push();
		// tweak settings
		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		GL11.glDisable(GL11.)
		UtilGl.applyBlendFunc(blendFunc);
//		rglContext.setTexture2d(true);
//		rglContext.setLightEnabled(false);
//		rglContext.setDepthTest(false);
//		rglContext.setBlendFunc(blendFunc);
//		rglContext.apply();
		UtilGl.setColor(color);
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV);
//		rglContext.pop();
	}
	/**
	 * Render the ractangle using its self settings of color and blendfunc.
	 * @param rglContext
	 * @param targetRectangle
	 * @param sourceRectangle
	 */
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle2d targetRectangle,
			TargetRectangle2d sourceRectangle) {
		float maxU = sourceRectangle.right;//getBottomRight().getX();
		float maxV = sourceRectangle.y;//getTopLeft().getY();
		float minU = sourceRectangle.x;//getTopLeft().getX();
		float minV = sourceRectangle.bottom;//getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		rglContext.push();
		// tweak settings
//		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		GL11.glDisable(GL11.)
//		UtilGl.applyBlendFunc(blendFunc);
		rglContext.setTexture2d(true);
		rglContext.setLightEnabled(false);
		rglContext.setDepthTest(false);
		rglContext.setBlendFunc(blendFunc);
		rglContext.apply();
		UtilGl.setColor(color);
		// activate the image texture
		bindThisTexture();
		if(samplingNear)
		{
			setNearestSampling(samplingNear);
		}
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV);
		if(samplingNear)
		{
			setNearestSampling(false);
		}
		rglContext.pop();
	}

	private void setNearestSampling(boolean samplingNear) {
		if(samplingNear)
		{
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_NEAREST);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_NEAREST);
		}else
		{
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		}
	}

	/**
	 * Render the texture on the GL context
	 *
	 * @param targetRectangle
	 *            the target rectangle to render the image in
	 * @param sourceRectangle
	 *            the part of the image to render on the target rectangle
	 * @param blendFunc
	 *            the blend function to use with alpha blending
	 */
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle targetRectangle, TargetRectangle sourceRectangle,
			EBlendFunc blendFunc) {
		float maxU = sourceRectangle.getBottomRight().getX();
		float maxV = sourceRectangle.getTopLeft().getY();
		float minU = sourceRectangle.getTopLeft().getX();
		float minV = sourceRectangle.getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		rglContext.push();
		// tweak settings
		rglContext.setTexture2d(true);
		rglContext.setLightEnabled(false);
		rglContext.setDepthTest(false);
		rglContext.setBlendFunc(blendFunc);
		rglContext.apply();
		GL11.glColor4f(1, 1, 1, 1); // no color
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV);
		rglContext.pop();
	}
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle2d targetRectangle, TargetRectangle2d sourceRectangle,
			EBlendFunc blendFunc) {
		float maxU = sourceRectangle.right; //getBottomRight().getX();
		float maxV = sourceRectangle.y; //getTopLeft().getY();
		float minU = sourceRectangle.x; //getTopLeft().getX();
		float minV = sourceRectangle.bottom; //getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		rglContext.push();
		// tweak settings
		rglContext.setTexture2d(true);
		rglContext.setLightEnabled(false);
		rglContext.setDepthTest(false);
		rglContext.setBlendFunc(blendFunc);
		rglContext.apply();
		GL11.glColor4f(1, 1, 1, 1); // no color
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV);
		rglContext.pop();
	}
	
	/**
	 * Disable all texture selection - select texture id 0 instead of real textures before drawing.
	 * Use only for performance measurement purpose.
	 */
	public static boolean disableAllTextures;
	
	private void bindThisTexture() {
		if(disableAllTextures)
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}else
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		}
	}

	/**
	 * Render the texture on the GL context
	 *
	 * @param targetRectangle
	 *            the target rectangle to render the image in
	 * @param sourceRectangle
	 *            the part of the image to render on the target rectangle
	 * @param blendFunc
	 *            the blend function to use with alpha blending
	 * @param colors colors of the points: topLeft, topRight, bottomLeft, bottomRight
	 */
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle targetRectangle, TargetRectangle sourceRectangle,
			EBlendFunc blendFunc, Vector4f[] colors) {
		float maxU = sourceRectangle.getBottomRight().getX();
		float maxV = sourceRectangle.getTopLeft().getY();
		float minU = sourceRectangle.getTopLeft().getX();
		float minV = sourceRectangle.getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		rglContext.push();
		// tweak settings
		rglContext.setTexture2d(true);
		rglContext.setLightEnabled(false);
		rglContext.setDepthTest(false);
		rglContext.setBlendFunc(blendFunc);
		rglContext.apply();
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV, colors);
		rglContext.pop();
	}
	public void drawTextureOnRectangle(RGlContext rglContext,
			TargetRectangle2d targetRectangle, TargetRectangle2d sourceRectangle,
			EBlendFunc blendFunc, Vector4f[] colors) {
		float maxU = sourceRectangle.right;//getBottomRight().getX();
		float maxV = sourceRectangle.y;//getTopLeft().getY();
		float minU = sourceRectangle.x;//getTopLeft().getX();
		float minV = sourceRectangle.bottom;//getBottomRight().getY();
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		rglContext.push();
		// tweak settings
		rglContext.setTexture2d(true);
		rglContext.setLightEnabled(false);
		rglContext.setDepthTest(false);
		rglContext.setBlendFunc(blendFunc);
		rglContext.apply();
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(targetRectangle, maxU, maxV, minU, minV, colors);
		rglContext.pop();
	}

	public void drawTextureOnRectangle(TargetRectangle rectangle, boolean blend) {
		drawTextureOnRectangle(rectangle, blend, new Vector4f(1,1,1,1));
	}

	public void drawTextureOnRectangle(TargetRectangle rectangle, boolean blend,
			Vector4f color) {
		float maxU = 1.0f;
		float maxV = 0.0f;
		float minU = 0.0f;
		float minV = 1.0f;
		// preserve settings
		// GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		// tweak settings

		// save current GL state
//		boolean _text = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
//		boolean _light = GL11.glGetBoolean(GL11.GL_LIGHTING);
//		boolean _depth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
//		boolean _blend = GL11.glGetBoolean(GL11.GL_BLEND);
//		int _blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
//		int _blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
//		ByteBuffer _colors = ByteBuffer.allocateDirect(16 * 4); // 16 * 32 bits
		// ^ of course we need only 4 * 4 bits, but LWJGL thinks it otherwise
//		FloatBuffer _colorsF = _colors.asFloatBuffer();
//		_colorsF.put(new float[] { 1, 1, 1, 1}); _colorsF.rewind();
//		GL11.glGetFloat(GL11.GL_CURRENT_COLOR, _colorsF);

		GL11.glEnable(GL11.GL_TEXTURE_2D); // be sure textures are on
		UtilGl.setColor(color);
		GL11.glDisable(GL11.GL_LIGHTING); // no lighting
		//GL11.glDisable(GL11.GL_DEPTH_TEST); // no depth test
		if (blend) {
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			GL11.glDisable(GL11.GL_BLEND); // disable transparency
		}
		// activate the image texture
		bindThisTexture();
		// draw a textured quad
		drawTextureOnRectangle(rectangle, maxU, maxV, minU, minV);
		// return to previous settings
		// GL11.glPopAttrib();

		// clean up after ourselves
//		if (!_text) GL11.glDisable(GL11.GL_TEXTURE_2D);
//		if (_light) GL11.glEnable(GL11.GL_LIGHTING);
//		if (_depth) GL11.glEnable(GL11.GL_DEPTH_TEST);
//		if (_blend) {
//			GL11.glEnable(GL11.GL_BLEND);
//		} else {
//			GL11.glDisable(GL11.GL_BLEND);
//		}
//		GL11.glBlendFunc(_blendSrc, _blendDst);
//		_colorsF.rewind();
//		GL11.glColor4f(_colorsF.get(), _colorsF.get(),
//				_colorsF.get(), _colorsF.get());
	}

	/**
	 * Renders the texture onto a specified surface ( {@link ITessellatedSurface} ).
	 * 
	 * @param blend
	 * @param color
	 * @param maxU
	 * @param maxV
	 * @param minU
	 * @param minV
	 * @param surface
	 */
	public void drawTextureOnSurface(boolean blend,
			Vector4f color,float maxU,
	float maxV ,
	float minU ,
	float minV ,ITessellatedSurface surface) {
		
		GL11.glEnable(GL11.GL_TEXTURE_2D); // be sure textures are on
		UtilGl.setColor(color);
		GL11.glDisable(GL11.GL_LIGHTING); // no lighting
		//GL11.glDisable(GL11.GL_DEPTH_TEST); // no depth test
		if (blend) {
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			GL11.glDisable(GL11.GL_BLEND); // disable transparency
		}
		// activate the image texture
		bindThisTexture();
	
		
		int nrOfStepU = surface.getTesselU();
		int nrOfStepV = surface.getTesselV();
		
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0;i < nrOfStepU;i++ ){
			float u0 = (float)i / (float) nrOfStepU;
			float u1 = (float)(i+1) / (float) nrOfStepU;
			for (int j =0; j <nrOfStepV ; j++){
				float v0 = (float)j / (float) nrOfStepV;
				float v1 = (float)(j+1) / (float) nrOfStepV;
				
				Vector3f p = surface.getPointAt (u0,v0);
				GL11.glTexCoord2f(u0, v0);
				GL11.glVertex3f(p.x,p.y,p.z );
				
				p = surface.getPointAt (u0,v1);
				GL11.glTexCoord2f(u0, v1);
				GL11.glVertex3f(p.x,p.y,p.z );
				 
				GL11.glTexCoord2f(u1, v1);
				p = surface.getPointAt (u1,v1);
				GL11.glVertex3f(p.x,p.y,p.z );
				
				GL11.glTexCoord2f(u1, v0);
				p = surface.getPointAt (u1,v0);
				GL11.glVertex3f(p.x,p.y,p.z );
			}
		}
		GL11.glEnd();
	}
	private void drawTextureOnRectangle(TargetRectangle rectangle, float maxU, float maxV, float minU, float minV,
			Vector4f[] colors) {
		GL11.glBegin(GL11.GL_QUADS);
		{
			UtilGl.setColor(colors[2]);
			GL11.glTexCoord2f(minU, minV);
			UtilGl.loadVertex(rectangle.getBottomLeft());

			UtilGl.setColor(colors[3]);
			GL11.glTexCoord2f(maxU, minV);
			UtilGl.loadVertex(rectangle.getBottomRight());

			UtilGl.setColor(colors[1]);
			GL11.glTexCoord2f(maxU, maxV);
			UtilGl.loadVertex(rectangle.getTopRight());

			UtilGl.setColor(colors[0]);
			GL11.glTexCoord2f(minU, maxV);
			UtilGl.loadVertex(rectangle.getTopLeft());
		}
		GL11.glEnd();
	}
	private void drawTextureOnRectangle(TargetRectangle2d rectangle, float maxU, float maxV, float minU, float minV,
			Vector4f[] colors) {
		GL11.glBegin(GL11.GL_QUADS);
		{
			UtilGl.setColor(colors[2]);
			GL11.glTexCoord2f(minU, minV);
			GL11.glVertex2f(rectangle.x, rectangle.bottom);
//			UtilGl.loadVertex(rectangle.getBottomLeft());

			UtilGl.setColor(colors[3]);
			GL11.glTexCoord2f(maxU, minV);
			GL11.glVertex2f(rectangle.right, rectangle.bottom);
//			UtilGl.loadVertex(rectangle.getBottomRight());

			UtilGl.setColor(colors[1]);
			GL11.glTexCoord2f(maxU, maxV);
			GL11.glVertex2f(rectangle.right, rectangle.y);

//			UtilGl.loadVertex(rectangle.getTopRight());

			UtilGl.setColor(colors[0]);
			GL11.glTexCoord2f(minU, maxV);
			GL11.glVertex2f(rectangle.x, rectangle.y);
//
//			UtilGl.loadVertex(rectangle.getTopLeft());
		}
		GL11.glEnd();
	}

	/**
	 * This method does not bind the texture!
	 * @param rectangle
	 * @param maxU
	 * @param maxV
	 * @param minU
	 * @param minV
	 */
	private void drawTextureOnRectangle(TargetRectangle rectangle, float maxU, float maxV, float minU, float minV) {
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(minU, minV);
			UtilGl.loadVertex(rectangle.getBottomLeft());

			GL11.glTexCoord2f(maxU, minV);
			UtilGl.loadVertex(rectangle.getBottomRight());

			GL11.glTexCoord2f(maxU, maxV);
			UtilGl.loadVertex(rectangle.getTopRight());

			GL11.glTexCoord2f(minU, maxV);
			UtilGl.loadVertex(rectangle.getTopLeft());
		}
		GL11.glEnd();
	}
	private void drawTextureOnRectangle(TargetRectangle2d rectangle, float maxU, float maxV, float minU, float minV) {
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(minU, minV);
			GL11.glVertex2f(rectangle.x, rectangle.bottom);
//			UtilGl.loadVertex(rectangle.getBottomLeft());

			GL11.glTexCoord2f(maxU, minV);
			GL11.glVertex2f(rectangle.right, rectangle.bottom);
//			UtilGl.loadVertex(rectangle.getBottomRight());

			GL11.glTexCoord2f(maxU, maxV);
			GL11.glVertex2f(rectangle.right, rectangle.y);
//			UtilGl.loadVertex(rectangle.getTopRight());

			GL11.glTexCoord2f(minU, maxV);
			GL11.glVertex2f(rectangle.x, rectangle.y);
//			UtilGl.loadVertex(rectangle.getTopLeft());
		}
		GL11.glEnd();
	}

	public void selectTecture() {
		// tweak settings
		GL11.glEnable(GL11.GL_TEXTURE_2D); // be sure textures are on
		// activate the image texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
	}

	public void deSelectTecture() {
		// tweak settings
		GL11.glDisable(GL11.GL_TEXTURE_2D); // be sure textures are on
	}

	private int formatInt;
	private int border;

	public int getFormatInt() {
		return formatInt;
	}

	public int hasBorder() {
		return border;
	}

	public void replaceContent(NativeImage newimage) {
		replaceContent(newimage, EMipMapType.none, ETextureWrapType.mirroredRepeat, false);
	}
	
	public void replaceContent(NativeImage newimage, EMipMapType mtype, ETextureWrapType wrapType, boolean appleClientStorage) {
		this.sourceImage=newimage;
		this.sourceMipmapType=mtype;
		this.sourceTextureWrapType=wrapType;
		if(EMipMapType.standard.equals(mtype))
		{
			ContextCapabilities cc=GLContext.getCapabilities();
			if(!cc.OpenGL30&&!cc.GL_EXT_framebuffer_object)
			{
				mtype=EMipMapType.none;
			}
		}
		this.width = newimage.getWidth();
		this.height = newimage.getHeight();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		switch (wrapType) {
		case clamp:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL11.GL_CLAMP);
			break;
		case clampYrepeatX:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL11.GL_CLAMP);
			break;
		case clampXrepeatY:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL11.GL_REPEAT);
			break;
		case repeat:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL11.GL_REPEAT);
			break;
		case mirroredRepeat:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL14.GL_MIRRORED_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL14.GL_MIRRORED_REPEAT);
			break;
		default:
			break;
		}
		switch (mtype) {
		case none:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_LINEAR);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR);
			break;
		case standard:
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);
			// In GL 3.x, GL_GENERATE_MIPMAP is deprecated. You must use glGenerateMipmap.
//				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP,
//					GL11.GL_TRUE);
			break;
		default:
			throw new RuntimeException("mipmapping type is not implemented: "+mtype);
		}
		int alignment=newimage.getAlignment();
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, alignment);
		int pixel_format;
		int dataformat;
		pixel_format=componentOrderToOGL(newimage.getComponentOrder());
		int internal_format=componentOrderTOIntarnalFormat(newimage.getComponentOrder());
		boolean undoClientStorage=false;
		if(appleClientStorage)
		{
			ContextCapabilities cc=GLContext.getCapabilities();
			if(cc.GL_APPLE_client_storage)
			{
				System.out.println("Apple client storage!");
				GL11.glPixelStorei(APPLEClientStorage.GL_UNPACK_CLIENT_STORAGE_APPLE, GL11.GL_TRUE);
				undoClientStorage=true;
			}
		}
		dataformat = GL11.GL_UNSIGNED_BYTE;
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internal_format, width, height,
				0, pixel_format, dataformat, newimage.getBuffer().getJavaAccessor());
		switch (mtype) {
		case none:
			break;
		case standard:
			// Using GL_GENERATE_MIPMAP instead causes my NVidia to process about 100%
			// CPU! Seems to use CPU to generate mipmap levels
			if(GLContext.getCapabilities().OpenGL30)
			{
				GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			}else
			{
				EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
			}
			break;
		}
		// Reset the default behaviour
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP,
//				GL11.GL_FALSE);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		if(undoClientStorage)
		{
			GL11.glPixelStorei(APPLEClientStorage.GL_UNPACK_CLIENT_STORAGE_APPLE, GL11.GL_FALSE);
		}
	}
	public static int componentOrderToOGL(ENativeImageComponentOrder co)
	{
		switch (co) {
		case RGB:
			return GL11.GL_RGB;
		case RGBA:
			return GL11.GL_RGBA;
		case MONO:
			return GL11.GL_LUMINANCE;
		case ALPHA:
			return GL11.GL_ALPHA;
		case BGR:
			return GL12.GL_BGR;
		case BGRA:
			return GL12.GL_BGRA;
		case ARGB:
			return GL12.GL_BGRA;
		default:
			throw new RuntimeException(
					"pixel format is unknown to opengl module: "
							+ co);
		}
	}

	private int componentOrderTOIntarnalFormat(
			ENativeImageComponentOrder componentOrder) {
		switch (componentOrder) {
		case ARGB:
		case BGRA:
		case RGBA:
			return GL11.GL_RGBA8;
		case BGR:
		case RGB:
			return GL11.GL_RGB8;
		case ALPHA:
			return GL11.GL_ALPHA8;
		case MONO:
			return GL11.GL_LUMINANCE8;
		default:
			throw new RuntimeException(
					"pixel format is unknown to opengl module: "
							+ componentOrder);
		}
	}

	/**
	 * Generate mip-map for an existing texture. It is useful when the texture is rendered shrinked.
	 */
	public void generateMipMap()
	{
		ContextCapabilities cc=GLContext.getCapabilities();
		if(cc.OpenGL30)
		{
			selectTecture();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			deSelectTecture();
		}else if(cc.GL_EXT_framebuffer_object)
		{
			selectTecture();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
			EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
			deSelectTecture();
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public void applyBlendFunc(ENativeImageAlphaStorageFormat alphaFormat) {
		if(ENativeImageAlphaStorageFormat.premultiplied.equals(alphaFormat))
		{
			setBlendFunc(EBlendFunc.ALPHA_PREMULTIPLIED);
		}else
		{
			setBlendFunc(EBlendFunc.SRC_ALPHA__ONE_MINUS_SRC_ALPHA);
		}		
	}

	public void setSamplingNear(boolean samplingNear) {
		this.samplingNear=samplingNear;
	}
}
