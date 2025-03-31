package hu.qgears.opengl.commons;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Render on texture functionality using 
 * using EXTFramebufferObject
 * (instead of OpenGL 3.0 - framebuffer object.)
 * 
 * TODO - leaks memory after dispose - allocate a single one and reuse!
 * 
 * @author adam, rizsi
 *
 */
public class RenderOnTextureFBO extends AbstarctRenderOnTexture implements IRenderOnTexture {
	private Texture target;	
	private boolean disposed=false;

	private boolean depthBuffer=false;
	private int rbId=-1;
	private int fbId=-1;
	private static int numberOfROT;
	private static int rotCreated;

	private static void textureCreated() {
		numberOfROT++;
		rotCreated++;
	}
	private static void textureDisposed() {
		numberOfROT--;
	}
	
	
	/**
	 * @since 6.0
	 */
	public static int getNumberOfROT() {
		return numberOfROT;
	}
	
	/**
	 * @since 6.0 
	 */
	public static int getRotCreated() {
		return rotCreated;
	}
	
	public RenderOnTextureFBO(Texture target, boolean needDephBuffer) throws LWJGLException
	{
		this.target=target;
		this.depthBuffer=needDephBuffer;
		init(target.getSize());
		textureCreated();
	}
	private void init(SizeInt size) throws LWJGLException
	{
		this.size=size;
		fbId=EXTFramebufferObject.glGenFramebuffersEXT();
		if(depthBuffer)
		{
			bind(true);
			createDepthBuffer();
			unbind();
		}
	}
	@Override
	protected void bind(boolean allowNull)
	{
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbId);
		if(target==null)
		{
			if(!allowNull)
			{
				throw new RuntimeException("Target texture is not set!");
			}
			return;
		}
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, 
				GL11.GL_TEXTURE_2D, 
				target.getTextureHandle(), 0);
	}
	@Override
	protected void unbind()
	{
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, 
				GL11.GL_TEXTURE_2D, 
				0, 0);
		
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
	}

	private void createDepthBuffer() {
		if (depthBuffer) {
			rbId = EXTFramebufferObject.glGenRenderbuffersEXT();
			// Bind renderbuffer
			EXTFramebufferObject.glBindRenderbufferEXT(
					EXTFramebufferObject.GL_RENDERBUFFER_EXT, rbId);

			// Init as a depth buffer
			EXTFramebufferObject.glRenderbufferStorageEXT(
					EXTFramebufferObject.GL_RENDERBUFFER_EXT,
					GL30.GL_DEPTH_COMPONENT32F, size.getWidth(),
					size.getHeight());
			// Attach to the FBO for depth
			EXTFramebufferObject.glFramebufferRenderbufferEXT(
					EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
					EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
					EXTFramebufferObject.GL_RENDERBUFFER_EXT, rbId);
		}
	}
	public void dispose()
	{
		if(rbId!=-1)
		{
			EXTFramebufferObject.glDeleteRenderbuffersEXT(rbId);
			rbId=-1;
		}
		if(fbId!=-1)
		{
			EXTFramebufferObject.glDeleteFramebuffersEXT(fbId);
			fbId=-1;
		}
		if(!disposed)
		{
			textureDisposed();
		}
		disposed=true;
	}
	/**
	 * Render scene on a texture.
	 * @param glContext openGL context that stores the current state of oepnGL
	 * @param renderMethod callback that does the rendering
	 */
	public void render(RGlContext glContext, IOnTextureRenderer renderMethod)
	{
		render(glContext, renderMethod, null);
	}
	/**
	 * 
	 * @return The texture this render on texture renders on.
	 */
	public Texture getTarget() {
		return target;
	}
	/**
	 * 
	 * @param target The texture this render on texture renders on.
	 */
	public void setTarget(Texture target) {
		this.target = target;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}
	@Override
	protected void stealImageData(RGlContext ctx, NativeImage out) {
		GL11.glFinish();
		int alignment=out.getAlignment();
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, alignment);
		int format=Texture.componentOrderToOGL(out.getComponentOrder());
		GL11.glReadBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
		out.getBuffer().getJavaAccessor().clear();
		GL11.glReadPixels(0,0, out.getWidth(), out.getHeight(), format, GL11.GL_UNSIGNED_BYTE, out.getBuffer().getJavaAccessor()); 
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
	}
}
