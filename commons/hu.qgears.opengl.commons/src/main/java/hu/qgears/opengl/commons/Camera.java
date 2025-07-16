package hu.qgears.opengl.commons;


import hu.qgears.opengl.commons.input.IKeyboard;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

/**
 * Tér minden irányába mozgatható kamera.
 * <p>
 * Can also be used as a context of an element.
 * 
 * @author rizsi
 *
 */
public class Camera {
	public Camera() {
	}
	public Camera(Vector3f position, Vector3f forward, Vector3f up) {
		super();
		this.position = position;
		this.forward = forward;
		this.up = up;
	}
	public Camera(Camera toCopy) {
		this.fov=toCopy.fov;
		this.aspectCorrection=toCopy.aspectCorrection;
		this.position=toCopy.position;
		this.up=toCopy.up;
		this.forward=toCopy.forward;
	}
	private float fov=30;
	private float aspectCorrection=1;
	private long lastProcessKeyboard=-1;

	// A kamera pozíciója a játék adott pillanatában
	private Vector3f position=new Vector3f(0,0,0);
	private Vector3f up=new Vector3f(1.0f,0,0);
	private Vector3f forward=new Vector3f(0,1.0f,0);
	public Vector3f getPosition() {
		return position;
	}
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	public Vector3f getUp() {
		return up;
	}
	public void setUp(Vector3f up) {
		this.up = up;
	}
	public Vector3f getForward() {
		return forward;
	}
	public void setForward(Vector3f forward) {
		this.forward = forward;
	}

	public void setCamera() {
		// A projekciós mátrixot szorozzuk jobbról
		// (tehát a projektció előtt fogjuk végrehajtani)
		// azzal a transzformációval, ami a kamerát a kívánt pozícióba viszi
		UtilGl.rotate(GL11.GL_PROJECTION,
				90,
				new Vector3f(0,0,1));
		UtilGl.rotate(GL11.GL_PROJECTION,
				-90,
				new Vector3f(1,0,0));
		up.normalise();
		forward.normalise();
		UtilGl.transformToCoordinateSystem(GL11.GL_PROJECTION, up, forward);
		UtilGl.translate(GL11.GL_PROJECTION, UtilGl.negate(position));
	}
	public void turnUp(float f) {
		Vector3f left=new Vector3f();
		Vector3f.cross(up, forward, left);
		forward=UtilGl.turnAround(forward, left, f);
		up=UtilGl.turnAround(up, left, f);
		normalizeCamera();
	}
	public void turnAroundForward(float f) {
		up=UtilGl.turnAround(up, forward, f);
		normalizeCamera();
	}
	public void turnAroundVector(Vector3f axis, float f) {
		up=UtilGl.turnAround(up, axis, f);
		forward=UtilGl.turnAround(forward, axis, f);
		normalizeCamera();
	}
	private void normalizeCamera() {
		Vector3f left=new Vector3f();
		Vector3f.cross(up, forward, left);
		Vector3f.cross(left, up, forward);
		up.normalise();
		forward.normalise();
	}
	public void turnRight(float f) {
		forward=UtilGl.turnAround(forward, up, -f);
		normalizeCamera();
	}
	public void moveRight(float speed) {
		Vector3f left=new Vector3f();
		Vector3f.cross(up, forward, left);
		Vector3f dir=new Vector3f(left);
		position=UtilGl.add(position,
				(Vector3f)dir.scale(-speed));
	}
	public void moveForward(float speed) {
		Vector3f dir=new Vector3f(forward);
		position=UtilGl.add(position,
				(Vector3f)dir.scale(speed));
	}
	public void moveUp(float speed) {
		Vector3f dir=new Vector3f(up);
		position=UtilGl.add(position,
				(Vector3f)dir.scale(speed));
	}

	public void processKeyboard(IKeyboard keyboard,
			boolean invertmouse, long time)
	{
			float speed = 60;
			
			float timeMultiplier=(float)(time-lastProcessKeyboard)/1000;
			speed*=timeMultiplier;
			if (lastProcessKeyboard > 0) {
//				// Csak akkor mozgatjuk egérrel a kamerát ha mienk az egér
//				if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
//					moveRight(speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
//					moveRight(-speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
//					moveForward(speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
//					moveForward(-speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
//					moveUp(speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
//					moveUp(-speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
//					turnAroundForward(-speed);
//				}
//				if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
//					turnAroundForward(speed);
//				}
			}
			lastProcessKeyboard = time;
	}
	public void drawCrossbar()
	{
		float distance=5;
		float r=.1f;
		Vector3f crowBarPos=UtilGl.add(position, UtilGl.mul(forward, distance));
		Vector3f left=UtilGl.cross(up, forward);
		GL11.glDisable(GL11.GL_LIGHTING);
		{
			UtilGl.setColor(new Vector3f(1,1,1));
			GL11.glBegin(GL11.GL_LINES);
			{
				UtilGl.addVertex(UtilGl.add(crowBarPos,
							UtilGl.mul(left, r)));
				UtilGl.addVertex(UtilGl.add(crowBarPos,
						UtilGl.mul(left, -r)));
				UtilGl.addVertex(UtilGl.add(crowBarPos,
						UtilGl.mul(up, r)));
				UtilGl.addVertex(UtilGl.add(crowBarPos,
					UtilGl.mul(up, -r)));
			}
			// Befejeztük a háromszögek koordinátáit
			GL11.glEnd();
		}
	}
	public Vector3f getLeft() {
		return UtilGl.cross(up, forward);
	}
	/**
	 * Set the vectors:
	 *  * up: z
	 *  * forward: y
	 * 
	 */
	public void initToGameDefault()
	{
		up=new Vector3f(0,0,1f);
		forward=new Vector3f(0,1f,0);
	}
	public float getFov() {
		return fov;
	}
	public void setFov(float fov) {
		this.fov = fov;
	}
	public float getAspectCorrection() {
		return aspectCorrection;
	}
	public void setAspectCorrection(float aspectCorrection) {
		this.aspectCorrection = aspectCorrection;
	}
	/**
	 * Initialize OpenGL projection matrix state based on this
	 * camera.
	 */
	public void initProjection(int width, int height)
	{
		// OpenGL-ben általában float értékekkel számolunk mindent
		float w=width;
		float h=height;
		// Azt jelezzük az OpenGL felé, hogy a
		// projekciós (a tér síkra leképzése)
		// mátrixot (transzformációt) fogjuk állítani
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		// Egységmátrixot betöltjük, azaz nullázzuk a korábbi beállítást
		GL11.glLoadIdentity();
		// Az adott látószögű kamera beállítása
		// itt adjuk meg a képarányt és a legközelebbi, illetve
		// legtávolabbi látható pont távolságát is
		GLU.gluPerspective((float)fov, w/h*aspectCorrection, 1.0f,10000f);
		// Azt jelezzük az OpenGL felé, hogy a
		// model-view mátrixot állítjuk be
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		// egységmátrix betöltése, azaz a korábbiak törlése
		GL11.glLoadIdentity();
		// Beállítjuk, hogy a képernyő melyik részére rajzolunk
		// természetesen a teljes képernyőt használjuk
		GL11.glViewport(0, 0, width, height);
		setCamera();
	}
}
