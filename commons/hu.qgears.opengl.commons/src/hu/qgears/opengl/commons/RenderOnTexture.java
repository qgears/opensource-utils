package hu.qgears.opengl.commons;

import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.RGlContext;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;



/**
 * Render on texture functionality using OpenGL 3.0 - framebuffer object.
 * 
 * TODO - depth buffer leaks memory after dispose - allocate a single one and reuse!
 * 
 * @author rizsi
 *
 */
public class RenderOnTexture extends AbstarctRenderOnTexture implements IRenderOnTexture {
	private Texture target;	
	private boolean disposed=false;

	private boolean depthRequired=false;

	
	public RenderOnTexture(Texture target) throws LWJGLException
	{
		this.target=target;
		init(target.getSize());
	}
	public RenderOnTexture(SizeInt size) throws LWJGLException, IOException, InterruptedException, ExecutionException
	{
		init(size);
	}
	private int fbId;
	private void init(SizeInt size) throws LWJGLException
	{
		this.size=size;
		fbId=GL30.glGenFramebuffers();
		if(depthRequired)
		{
			bind(true);
			createDepthBuffer();
			unbind();
		}
	}
	@Override
	protected void bind(boolean allowNull)
	{
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbId);
		if(target==null)
		{
			if(!allowNull)
			{
				throw new RuntimeException("Target texture is not set!");
			}
			return;
		}
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, 
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
				target.getTextureHandle(), 0);
	}
	@Override
	protected void unbind()
	{
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, 
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	private int rbId;
	private void createDepthBuffer()
	{
		rbId=GL30.glGenRenderbuffers();
		// Bind renderbuffer
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbId);

		// Init as a depth buffer
		GL30.glRenderbufferStorage( GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32F, size.getWidth(), size.getHeight());

		// Attach to the FBO for depth
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				GL30.GL_RENDERBUFFER, rbId);
	}
	public void dispose()
	{
		if(depthRequired)
		{
			GL30.glDeleteRenderbuffers(rbId);
		}
		GL30.glDeleteFramebuffers(fbId);
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
	/**
	 * Instantiate render on texture mechanism.
	 * 
	 * Decides whether to use opengl 3.0 or old style extension mechanism!
	 * 
	 * @param targetTexture
	 * @return
	 * @throws LWJGLException
	 */
	public static IRenderOnTexture create(Texture targetTexture) throws LWJGLException {
		// We use the old-style API. It works on all drivers.
		return new RenderOnTextureFBO(targetTexture, false);
	}
	@Override
	protected void stealImageData(RGlContext ctx, NativeImage out) {
		GL11.glFinish();
		int alignment=out.getAlignment();
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, alignment);
		int format=Texture.componentOrderToOGL(out.getComponentOrder());
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		out.getBuffer().getJavaAccessor().clear();
		GL11.glReadPixels(0,0, out.getWidth(), out.getHeight(), format, GL11.GL_UNSIGNED_BYTE, out.getBuffer().getJavaAccessor()); 
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
	}
}
