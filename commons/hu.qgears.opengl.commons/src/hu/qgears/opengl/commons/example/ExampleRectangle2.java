package hu.qgears.opengl.commons.example;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.AbstractOpenglApplication2;
import hu.qgears.opengl.commons.Camera;
import hu.qgears.opengl.commons.EGLImplementation;
import hu.qgears.opengl.commons.IOnTextureRenderer;
import hu.qgears.opengl.commons.IRenderOnTexture;
import hu.qgears.opengl.commons.RenderOnTexture;
import hu.qgears.opengl.commons.TargetRectangle;
import hu.qgears.opengl.commons.Texture;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.context.RGlContext;
import hu.qgears.opengl.commons.input.IKeyboard;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Egyetlen háromszöget megjelenítő alkalmazás.
 * @author rizsi
 *
 */
public class ExampleRectangle2 extends AbstractOpenglApplication2 {
	Camera camera=new Camera();
	IRenderOnTexture rot;
	public ExampleRectangle2() {
		setImplementation(EGLImplementation.x11);
	}
	@Override
	protected void logic() {
	}
	@Override
	protected void render() {
		// A kép rajzolásának elkezdését jelezzük ezzel a paranccsal
		GL11.glRenderMode(GL11.GL_RENDER);
		// Beállítjuk a kamera pozícióját.
		// Innentől mindent úgy rajzol az OpenGL, hogy a megadott
		// Pozícióba képzeli a kamerát
		glInit3D(getSize().getWidth(), getSize().getHeight());
		camera.setCamera();
		// háttér törlése
		GL11.glClearColor(0,0,1,1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		// fehér háromszög rajzolása
		renderVideo();
	}
	private void renderVideo() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(0, 0, -100);
//			UtilGl.rotate(GL11.GL_MODELVIEW, 90, 0, -100);
			try{				
				rot.render(new RGlContext(), new IOnTextureRenderer() {
					
					@Override
					public void render(RGlContext glContext, SizeInt size) {
						glInit3D(getSize().getWidth(), getSize().getHeight());
						camera.setCamera();
						// háttér törlése
						GL11.glClearColor(0,1,0,.3f);
						GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
						
						int sizeX=1024;
						int sizeY=768;
						float scale=.2f;
						TargetRectangle rect=new TargetRectangle(
								(Vector2f)new Vector2f(-sizeX/2,sizeY/2).scale(scale),
								(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
						);
						UtilGl.drawRectangle(glContext, rect, new Vector4f(1,0,0,.5f));
						rect=new TargetRectangle(
								(Vector2f)new Vector2f(-sizeX/2+sizeX/2,sizeY/2).scale(scale),
								(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
						);
						UtilGl.drawRectangle(glContext, rect, new Vector4f(1,0,0,1f));
					}
				});
				int sizeX=getSize().getWidth();
				int sizeY=getSize().getHeight();
				float scale=.2f;
				TargetRectangle drawRect=new TargetRectangle(
						(Vector2f)new Vector2f(-sizeX/2,sizeY/2).scale(scale),
						(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
				);
				rot.getTarget().drawTextureOnRectangle(drawRect);
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
		GL11.glPopMatrix();
	}
	/**
	 * Test entry point
	 */
	public static void main(String[] args) {
		try {
			ExampleRectangle2 fswTest = new ExampleRectangle2();
			fswTest.execute();
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	protected void initialize() throws Exception {
		setWindowSize(new SizeInt(1024, 768));
		setInitTitle("Rectangle example");
//		setDefaultFullscreen(true);
		super.initialize();
		try
		{
			Texture target = Texture.create(getSize().getWidth(), getSize().getHeight());
			rot = RenderOnTexture.create(target);
			camera.setForward(new Vector3f(0,0,-1));
			camera.setUp(new Vector3f(0,1,0));
			camera.setPosition(new Vector3f(0,0,300));
		}catch(Throwable t)
		{
			throw new Exception(t);
		}
	}
	@Override
	protected boolean isDirty() {
		return true;
	}
	@Override
	protected void processKeyboard(IKeyboard keyboard) throws Exception {
		super.processKeyboard(keyboard);
		camera.processKeyboard(keyboard, false, System.currentTimeMillis());
	}
	@Override
	protected void logError(String message, Exception e) {
		e.printStackTrace();
	}
}
