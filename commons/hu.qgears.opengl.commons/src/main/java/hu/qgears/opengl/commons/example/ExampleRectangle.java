package hu.qgears.opengl.commons.example;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.AbstractOpenglApplication2;
import hu.qgears.opengl.commons.Camera;
import hu.qgears.opengl.commons.IOnTextureRenderer;
import hu.qgears.opengl.commons.IRenderOnTexture;
import hu.qgears.opengl.commons.RenderOnTexture;
import hu.qgears.opengl.commons.TargetRectangle;
import hu.qgears.opengl.commons.Texture;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.context.RGlContext;
import hu.qgears.opengl.commons.input.IKeyboard;

/**
 * Egyetlen háromszöget megjelenítő alkalmazás.
 * @author rizsi
 *
 */
public class ExampleRectangle extends AbstractOpenglApplication2 {
	private Camera camera=new Camera();
	private static final Logger LOG = Logger.getLogger(ExampleRectangle.class);
	private IRenderOnTexture rot;
	public ExampleRectangle() {
		setWindowSize(new SizeInt(1024, 768));
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
		glInit3D(getClientAreaSize().getWidth(), getClientAreaSize().getHeight());
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
			try{				
				rot.render(new RGlContext(), new IOnTextureRenderer() {
					
					@Override
					public void render(RGlContext glContext, SizeInt size) {
						glInit3D(getClientAreaSize().getWidth(), getClientAreaSize().getHeight());
						camera.setCamera();
						// háttér törlése
						GL11.glClearColor(0,1,0,.3f);
						GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
						
						float sizeX=640;
						float sizeY=480;
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
				float sizeX=getClientAreaSize().getWidth();
				float sizeY=getClientAreaSize().getHeight();
				float scale=.2f;
				TargetRectangle drawRect=new TargetRectangle(
						(Vector2f)new Vector2f(-sizeX/2,sizeY/2).scale(scale),
						(Vector2f)new Vector2f(sizeX-sizeX/2, -sizeY+sizeY/2).scale(scale)
				);
				rot.getTarget().drawTextureOnRectangle(drawRect);
			}
			catch (Throwable t) {
				LOG.error("Error while rendering",t);
			}
		}
		GL11.glPopMatrix();
	}
	@Override
	protected void keyDown(int eventKey, char ch, boolean shift, boolean ctrl,
			boolean alt, boolean special) throws Exception {
		switch (ch) {
		case 'm':
			// Az egeret eltüntetjük
//			Mouse.setGrabbed(!Mouse.isGrabbed());
			break;
		default:
			super.keyDown(eventKey, ch, shift, ctrl, alt, special);
			break;
		}
	}
	@Override
	protected void processKeyboard(IKeyboard keyboard) throws Exception {
		super.processKeyboard(keyboard);
		camera.processKeyboard(keyboard, false, System.currentTimeMillis());
	}
	/**
	 * Test entry point
	 */
	public static void main(String[] args) {
		try {
			ExampleRectangle fswTest = new ExampleRectangle();
			fswTest.execute();
			System.exit(0); //NOSONAR intentional exit point
		} catch (Exception e) {
			LOG.error("Error while executing application",e);
		}
	}
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		try
		{
			Texture target = Texture.create(getClientAreaSize().getWidth(),
					getClientAreaSize().getHeight());
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
	protected void logError(String message, Exception e) {
		LOG.error(message, e);
	}
}
