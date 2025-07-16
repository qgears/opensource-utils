package hu.qgears.opengl.commons;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.Rectangle;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.context.EBlendFunc;
import hu.qgears.opengl.commons.context.RGlContext;


/**
 * Helper methods for using OpenGL API.
 * 
 * @author rizsi
 *
 */
public class UtilGl {
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	private static ByteBuffer tempByteBuffer=UtilGl.allocBytes(512);
	private static IntBuffer tempIntBuffer=UtilGl.allocInts(16);
	// will be populated by extensionExists()
	private HashMap<String, String> OpenGLextensions;
	/**
	 * Flip rendering Y.
	 */
	public static boolean flipY=false;
	/**
	 * Return true if the OpenGL context supports the given OpenGL extension.
	 */
	public boolean extensionExists(String extensionName) {
		if (OpenGLextensions == null) {
			String[] GLExtensions = GL11.glGetString(GL11.GL_EXTENSIONS).split(" ");
			OpenGLextensions = new HashMap<String, String>();
			for (int i=0; i < GLExtensions.length; i++) {
				OpenGLextensions.put(GLExtensions[i].toUpperCase(),"");
			}
		}
		return (OpenGLextensions.get(extensionName.toUpperCase()) != null);
	}

	/**
	 * Allocate integers an a direct buffer.
	 * 
	 * @param howmany
	 * @return
	 */
	private static IntBuffer allocInts(int howmany) {
		return BufferUtils.createIntBuffer(howmany);
	}
	/**
	 * Allocate floats in a direct buffer.
	 * @param howmany
	 * @return
	 */
	private static FloatBuffer allocFloats(int howmany) {
		return BufferUtils.createFloatBuffer(howmany);
	}
	public static ByteBuffer allocBytes(int length) {
		ByteBuffer bb = ByteBuffer.allocateDirect(length).order(
				ByteOrder.nativeOrder());
		return bb;
	}
	/**
	 * Allocate bytes aligned with memory addresses.
	 * Aligned memory is required for some openCV methods.
	 * 
	 * @param length
	 * @param align
	 * @return
	 */
	public static ByteBuffer allocBytes(int length, int align) {
		ByteBuffer bb = ByteBuffer.allocateDirect(length).order(
				ByteOrder.nativeOrder());
		return bb;
	}
	public static void drawRectangle( //NOSONAR method has 9 parameters but is still comprehensible
			RGlContext rgl,
			Vector3f bottomLeft,
			Vector3f bottomRight,
			Vector3f topRight,
			Vector3f topLeft,
			byte r, byte g, byte b, byte a)
	{
		rgl.pushAndClear();
		rgl.setBlendFunc(EBlendFunc.SRC_ALPHA__ONE_MINUS_SRC_ALPHA);
		GL11.glColor4ub(r,g,b,a); // set desired color
		rgl.setDepthTest(false);
		rgl.setLightEnabled(false);
		rgl.apply();
		GL11.glBegin(GL11.GL_QUADS);
		{
			loadVertex(bottomLeft);

			loadVertex(bottomRight);

			loadVertex(topRight);

			loadVertex(topLeft);
		}
		GL11.glEnd();
		rgl.pop();
	}
	public static void drawRectangle(
			RGlContext rgl,
			TargetRectangle rect,
			Vector4f colorRGBA)
	{
		rgl.pushAndClear();
		rgl.setBlendFunc(EBlendFunc.SRC_ALPHA__ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(colorRGBA.x, colorRGBA.y, colorRGBA.z, colorRGBA.w); // set desired color
		rgl.setDepthTest(false);
		rgl.setLightEnabled(false);
		rgl.apply();
		GL11.glBegin(GL11.GL_QUADS);
		{
			loadVertex(rect.getBottomLeft());

			loadVertex(rect.getBottomRight());

			loadVertex(rect.getTopRight());

			loadVertex(rect.getTopLeft());
		}
		GL11.glEnd();
		rgl.pop();
	}
	public static void drawRectangle(
			RGlContext rgl,
			TargetRectangle2d rect,
			Vector4f colorRGBA)
	{
		rgl.pushAndClear();
		rgl.setBlendFunc(EBlendFunc.SRC_ALPHA__ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(colorRGBA.x, colorRGBA.y, colorRGBA.z, colorRGBA.w); // set desired color
		rgl.setDepthTest(false);
		rgl.setLightEnabled(false);
		rgl.apply();
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(rect.x, rect.bottom);

			GL11.glVertex2f(rect.right, rect.bottom);

			GL11.glVertex2f(rect.right, rect.y);

			GL11.glVertex2f(rect.x, rect.y);
		}
		GL11.glEnd();
		rgl.pop();
	}

	public static final void loadVertex(Vector3f p) {
		GL11.glVertex3f(p.x, p.y, p.z);
	}
	
	public static final void loadVertex(Vector2f p) {
		GL11.glVertex2f(p.x, p.y);
	}
	
	public static void pickMatrix(int width, int height, float x, float y,
			float size) {
		float translateSize=size;
		GL11.glScalef(((float)width)/size,((float)height/size),0);
		GL11.glTranslatef(-(x-translateSize/2),-(y-translateSize/2),0);
	}
	public static void popBothMatrixes()
	{
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
	}
	public static void pushBothMatrixes()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
	}
	public static void translate(int matrix, Vector3f translate) {
		GL11.glMatrixMode(matrix);
		GL11.glTranslatef(translate.x,translate.y, translate.z);
	}
	public static void multiply(int matrix, Matrix4f m) {
		GL11.glMatrixMode(matrix);
		FloatBuffer fb=tempFloatBuffer;
		fb.clear();
		m.store(fb);
		fb.flip();
		GL11.glMultMatrixf(fb);
	}
	/**
	 * 
	 * @param matrix
	 * @param angle angle in degrees!
	 * @param around
	 */
	public static void rotate(int matrix, float angle, Vector3f around) {
		GL11.glMatrixMode(matrix);
		GL11.glRotatef(angle, around.x, around.y, around.z);
	}
	public static Vector2f mathMiddle(Vector2f a, Vector2f b, float rate)
	{
		Vector2f targetSize=new Vector2f(); 
		Vector2f.add(((Vector2f)new Vector2f(a).scale(1.0f-rate)),
				((Vector2f)new Vector2f(b).scale(rate))
				, targetSize);
		return targetSize;
	}
	public static Vector3f mathMiddle(Vector3f a, Vector3f b, float rate)
	{
		Vector3f targetSize=new Vector3f(); 
		Vector3f.add(((Vector3f)new Vector3f(a).scale(1.0f-rate)),
				((Vector3f)new Vector3f(b).scale(rate))
				, targetSize);
		return targetSize;
	}
	private static int maxTextureSize=-1;
	/**
	 * @return
	 */
	public static int getMaxTextureSize() {
		if(maxTextureSize<0)
		{
			tempIntBuffer.clear();
			GL11.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, tempIntBuffer);
			maxTextureSize=tempIntBuffer.get(0);
		}
		return maxTextureSize;
	}
	/**
	 * A vektor negáltja (ellentettje)
	 * @param vector
	 * @return
	 */
	public static Vector3f negate(Vector3f vector) {
		Vector3f ret=new Vector3f();
		return vector.negate(ret);
	}
	public static Vector3f add(Vector3f cameraPosition, Vector3f scale) {
		Vector3f ret=new Vector3f();
		Vector3f.add(cameraPosition, scale, ret);
		return ret;
	}
	public static Vector3f sub(Vector3f cameraPosition, Vector3f scale) {
		Vector3f ret=new Vector3f();
		Vector3f.sub(cameraPosition, scale, ret);
		return ret;
	}
	public static Vector2f sub(Vector2f cameraPosition, Vector2f scale) {
		Vector2f ret=new Vector2f();
		Vector2f.sub(cameraPosition, scale, ret);
		return ret;
	}
	public static Vector2f add(Vector2f cameraPosition, Vector2f scale) {
		Vector2f ret=new Vector2f();
		Vector2f.add(cameraPosition, scale, ret);
		return ret;
	}
	public static Vector3f mul(Vector3f cameraPosition, float x) {
		Vector3f ret=new Vector3f(cameraPosition);
		ret.scale(x);
		return ret;
	}
	public static Vector2f mul(Vector2f cameraPosition, float x) {
		Vector2f ret=new Vector2f(cameraPosition);
		ret.scale(x);
		return ret;
	}
	public static Vector3f cross(Vector3f cameraPosition, Vector3f scale) {
		Vector3f ret=new Vector3f();
		Vector3f.cross(cameraPosition, scale, ret);
		return ret;
	}
	static FloatBuffer tempFloatBuffer=UtilGl.allocFloats(100000);
	/**
	 * Get the pre-allocated float buffer for temporary use.
	 * @return
	 */
	public static FloatBuffer getTempFloatBuffer() {
		return tempFloatBuffer;
	}
	/**
	 * Transformation that moves us to an other coordinate system
	 * @param matrixMode load the transformation into this opengl matrix
	 * @param up the up vector of the coordinate system
	 * @param forward the forward vector of the coordinate system.
	 */
	public static void transformToCoordinateSystem(int matrixMode,
			Vector3f up, Vector3f forward) {
		Vector3f left=new Vector3f();
		Vector3f.cross(up, forward, left);
		tempFloatBuffer.clear();
		FloatBuffer fb=tempFloatBuffer;
		
		fb.put(up.x);
		fb.put(forward.x);
		fb.put(left.x);
		fb.put(0);
		
		fb.put(up.y);
		fb.put(forward.y);
		fb.put(left.y);
		fb.put(0);
		
		fb.put(up.z);
		fb.put(forward.z);
		fb.put(left.z);
		fb.put(0);
		
		fb.put(0);
		fb.put(0);
		fb.put(0);
		fb.put(1);
		
		fb.flip();
		GL11.glMatrixMode(matrixMode);
		GL11.glMultMatrixf(fb);
	}
	/**
	 * Transformation that moves an object an other coordinate system.
	 * The matrix is the adjunct of transformToCoordinateSystem
	 * @param matrixMode load the transformation into this opengl matrix
	 * @param up the up vector of the coordinate system
	 * @param forward the forward vector of the coordinate system.
	 */
	public static void transformObjectToCoordinateSystem(int matrixMode,
			Vector3f up, Vector3f forward) {
		Vector3f left=new Vector3f();
		Vector3f.cross(up, forward, left);
		tempFloatBuffer.clear();
		FloatBuffer fb=tempFloatBuffer;
		
		fb.put(up.x);
		fb.put(up.y);
		fb.put(up.z);
		fb.put(0);
		
		fb.put(forward.x);
		fb.put(forward.y);
		fb.put(forward.z);
		fb.put(0);
		
		fb.put(left.x);
		fb.put(left.y);
		fb.put(left.z);
		fb.put(0);
		
		fb.put(0);
		fb.put(0);
		fb.put(0);
		fb.put(1);
		
		fb.flip();
		GL11.glMatrixMode(matrixMode);
		GL11.glMultMatrixf(fb);
	}
	/**
	 * Use RGlContext.setColor instead
	 * @param color
	 */
	public static final void setColor(Vector3f color) {
		GL11.glColor3f(color.x, color.y, color.z);
	}
	/**
	 * Use RGlContext.setColor instead
	 * @param color
	 */
	public static final void setColor(Vector4f color) {
		GL11.glColor4f(color.x, color.y, color.z, color.w);
	}
	/**
	 * Merőleges egységvektor keresése.
	 * @param axis
	 * @return
	 */
	public static Vector3f findMeroleges(Vector3f axis)
	{
		Vector3f i=new Vector3f();
		Vector3f.cross(axis, new Vector3f(1,0,0), i);
		if(i.length()<.5f)
		{
			Vector3f.cross(axis, new Vector3f(0,1,0), i);
			if(i.length()<.5f)
			{
				Vector3f.cross(axis, new Vector3f(0,1,0), i);
			}
		}
		// Hosszát egységre állítjuk
		i.normalise();
		return i;
	}
	/**
	 * vektor forgatása tengely körül
	 * @param x forgatandó vektor
	 * @param axis forgatás tengelye
	 * @param angle forgatás szöge fokokban
	 * @return
	 */
	public static Vector3f turnAround(Vector3f toRotate, Vector3f axis, float angle)
	{
		axis=new Vector3f(axis);
		axis.normalise();
		// Keressünk egy tetszőleges merőleges egységvektort
		Vector3f i=findMeroleges(axis);
		// Keressük meg a hozzá tartozó merőleges egységvektort
		Vector3f j=new Vector3f();
		Vector3f.cross(axis, i, j);
		// Számoljuk ki a vektor komponenseit az i, j, axis koordinátarendszerben
		float z=Vector3f.dot(axis, toRotate);
		float x=Vector3f.dot(i, toRotate);
		float y=Vector3f.dot(j, toRotate);
		// Hajtsuk végre a forgatást az x, y síkban
		double alpha=Math.PI*angle/180.0;
		float cos=(float)Math.cos(alpha);
		float sin=(float)Math.sin(alpha);
		float xt=cos*x-sin*y;
		float yt=sin*x+cos*y;
		Vector3f ret=new Vector3f(0,0,0);
		Vector3f ret2=new Vector3f(0,0,0);
		// A komponenseket kiszámoljuk az egyszerűség kedvéért helyben
		axis.scale(z);
		i.scale(xt);
		j.scale(yt);
		Vector3f.add(i, axis, ret);
		Vector3f.add(ret, j, ret2);
		return ret2;
	}
	
	/**
	 * Use RGlContext.setMaterial instead
	 * @param color
	 */
	public static void setMaterial(Vector3f color) {
		
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, wrapTemp(new float[]{color.x, color.y, color.z,1f}));
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, wrapTemp(new float[]{0, 0, 0,1f}));
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, wrapTemp(new float[]{color.x, color.y, color.z,1f}));
		UtilGl.setColor(color);
		GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 50f);
	}
	public static void addVertex(Vector3f coo) {
		GL11.glVertex3f(coo.x, coo.y, coo.z);
	}
	public static void addNormal(Vector3f coo) {
		GL11.glNormal3f(coo.x, coo.y, coo.z);
	}
	public static void setWireFrame(boolean b) {
		if(b)
		{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}else
		{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
	
	/**
	 * Decode the ByteBuffer into an UTF-8 String. The length of the ByteBuffer
	 * to read is determined by the first int in the supplied IntBuffer.
	 * 
	 * @param shaderInfoLogBuffer the ByteBuffer that contains textual information
	 * @param size the first int in this buffer represents the size of shaderInfoLogBuffer
	 * @return the UTF-8 contents of shaderInfoLogBuffer
	 */
	public static String decodeToString(ByteBuffer shaderInfoLogBuffer, IntBuffer size) {
		int s=size.get(0);
		byte[] bs=new byte[s];
		shaderInfoLogBuffer.get(bs, 0, bs.length);
		return new String(bs,CHARSET_UTF8);
	}
	public static FloatBuffer wrapTemp(float[] fs) {
		tempFloatBuffer.clear();
		for(float f: fs)
		{
			tempFloatBuffer.put(f);
		}
		tempFloatBuffer.flip();
		return tempFloatBuffer;
	}
	public static IntBuffer wrapTemp(int[] fs) {
		tempIntBuffer.clear();
		for(int f: fs)
		{
			tempIntBuffer.put(f);
		}
		tempIntBuffer.flip();
		return tempIntBuffer;
	}
	public static FloatBuffer wrapTemp(Vector4f vec) {
		tempFloatBuffer.clear();
		tempFloatBuffer.put(vec.x);
		tempFloatBuffer.put(vec.y);
		tempFloatBuffer.put(vec.z);
		tempFloatBuffer.put(vec.w);
		tempFloatBuffer.flip();
		return tempFloatBuffer;
	}
	/**
	 * Returns the String wrapped in a ByteBuffer in UTF-8 encoding.
	 * @param string
	 * @return
	 */
	public static ByteBuffer wrapTemp(String string) {
		tempByteBuffer.clear();
		tempByteBuffer.put(string.getBytes(CHARSET_UTF8));
		tempByteBuffer.put((byte)0);
		tempByteBuffer.flip();
		return tempByteBuffer;
	}
	public static IntBuffer wrapTemp(int fbId) {
		tempIntBuffer.clear();
		tempIntBuffer.put(fbId);
		return tempIntBuffer;
	}
	public static void addTexture(Vector2f textCoo) {
		GL11.glTexCoord2f(textCoo.x, textCoo.y);
	}
	public static float toDegree(float fov) {
		return (float)(fov*180.0/Math.PI);
	}
	public static void scale(int glModelview, float f) {
		GL11.glMatrixMode(glModelview);
		GL11.glScalef(f,f,f);
	}
	public static Vector3f to3f(Vector2f v2f) {
		return new Vector3f(v2f.x, v2f.y, 0);
	}
	public static boolean equals(Vector3f a, Vector3f b) {
		return a.x==b.x&&a.y==b.y&&a.z==b.z;
	}
	/**
	 * translate scale and translate back
	 * @param glModelview
	 * @param middle
	 * @param scale
	 */
	public static void scaleAround(int glModelview, Vector3f middle,
			Vector3f scale) {
		GL11.glMatrixMode(glModelview);
		GL11.glTranslatef(middle.x, middle.y, middle.z);
		GL11.glScalef(scale.x, scale.y, scale.z);
		GL11.glTranslatef(-middle.x, -middle.y, -middle.z);
	}
	public static void rotateAround(int glModelview, Vector3f middle, float angleInDegrees,
			Vector3f axis) {
		GL11.glMatrixMode(glModelview);
		GL11.glTranslatef(middle.x, middle.y, middle.z);
		GL11.glRotatef(angleInDegrees, axis.x, axis.y, axis.z);
		GL11.glTranslatef(-middle.x, -middle.y, -middle.z);
	}
	/**
	 * Converts the given integer bitfield to a 4 channel color vector. The
	 * bitfield is partitioned into 8-bit length parts : the first (most
	 * significant) byte is interpreted as the first color channel, the second
	 * byte as the second channel, and so on.
	 * <p>
	 * The color coordinates read from bitfield are converted to float values,
	 * by mapping them into [0f,1f] range.
	 * 
	 * @param color
	 * @return The color vector, with coordinates in [0f,1f] range
	 */
	public static Vector4f toColor4f(int color) {
		return new Vector4f(toFloat(color >> 24),
				toFloat(color >>16),
				toFloat(color >> 8),
				toFloat(color));
	}
	
	/**
	 * Converts the color coordinate specified as the last 8 bit of given
	 * integer to a float number in range [0,1] (linear mapping of [0,255] range
	 * to [0,1] range).
	 * 
	 * @param channel
	 *            the integer from [0,255] range. Only the last 8 bit is taken
	 *            into consideration (large numbers are automatically
	 *            truncated).
	 * @return
	 */
	private static float toFloat(int channel){
		return (channel & 0xFF) / 255f;
	}
	
	/**
	 * Initialize a 2d mapping to the view:
	 *
	 * (0,0) is the middle of the view
	 * 1 pixel is 1 in the model
	 * x is directed right, y is directed up
	 */
	public static void initOrtho2d(SizeInt size) {
		int width = size.getWidth();
		int height = size.getHeight();
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(0, width, 0, height);
		/*for now, this is the intended behavior*/
		GL11.glTranslatef(width/2, height/2, 0); //NOSONAR 
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	public static void init3D(int width, int height, float fov,
			float aspectCorrection) {
		float w=width;
		float h=height;
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective((float)fov, w/h*aspectCorrection, 1.0f,10000f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, width, height);
	}
	/**
	 * Calculates the gradient color between color1 and color2 
	 * @param color1
	 * @param color2
	 * @param alpha 0..1, If 0 color1, if 1color2 will be returned
	 * @return
	 */
	public static Vector4f gradientColor(Vector4f color1,Vector4f color2, float alpha) {
		Vector4f grad = new Vector4f(color1);
		grad.scale(1-alpha);
		grad.translate(color2.x*alpha, color2.y*alpha, color2.z*alpha, color2.w*alpha);
		return grad;
	}
	/**
	 * Draw a minimal scene that is visible in an uninitialized OpenGL context!
	 */
	public static void drawMinimalScene() {
		GL11.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT );
		float theta=0;
		GL11.glPushMatrix();
		GL11.glRotatef( theta, 0.0f, 0.0f, 1.0f );
		GL11.glBegin( GL11.GL_TRIANGLES );
		
		GL11.glColor3f( 1.0f, 0.0f, 0.0f );
		GL11.glVertex2f( 0.0f, 1.0f );
		
		GL11.glColor3f( 0.0f, 1.0f, 0.0f );
		GL11.glVertex2f( 0.87f, -0.5f );
		
		GL11.glColor3f( 0.0f, 0.0f, 1.0f );
		GL11.glVertex2f( -0.87f, -0.5f );
		
		GL11.glEnd();
		GL11.glPopMatrix();
	}
	public static void glInit3D(RGlContext ctx, int clientAreaWidth, int clientAreaHeight, int appWidth, int appHeight) {
		glInit3D(ctx, clientAreaWidth, clientAreaHeight, appWidth, appHeight, null);
	}

	/**
	 * Initializes matrixes for 3D rendering
	 */
	public static void glInit3D(RGlContext ctx, int clientAreaWidth,
			int clientAreaHeight, int appWidth, int appHeight, Vector3f scale) {
		
		float w=appWidth;
		float h=appHeight;
		// Go into 3D projection mode.
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		double fov=30.0;
		double heightDivDistance=Math.tan(fov*Math.PI/180.0/2.0)*2.0;
		double big=1;
		heightDivDistance*=big;
		GLU.gluPerspective((float)fov,(w)/(h),h/2f,h*8);
		if(scale!=null)
		{
			GL11.glScalef(scale.x, scale.y, scale.z);
		}
		if(flipY)
		{
			GL11.glScalef(1f, -1f, 1f);
		}
		// We have to push the layer backwards so we form a
		// equal side triangle
		double distance=-(appHeight)/heightDivDistance;
		GL11.glTranslatef(0,0, (float)distance);
		// 0,0 is the middle of the screen
		GL11.glTranslatef(-w/2,-h/2, 0f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		if(ctx!=null)
		{
			ctx.setDefaultViewPort(setViewPort(appWidth, appHeight, clientAreaHeight, ctx.getStoredViewPort()));
		}
	}
	/**
	 * Setup the OpenGL viewport and save the viewport parameters set into a
	 * Rectangle object. This rectangle object can be used to reset the viewport
	 * in case it is ruined by a sub-renderer.
	 * @param width
	 * @param height
	 * @param clientAreaHeight
	 * @return
	 */
	public static Rectangle setViewPort(int width, int height, int clientAreaHeight, Rectangle ret)
	{
		int y0=clientAreaHeight-height;
		int xadd=0;
		int yadd=0;
		GL11.glViewport(xadd, yadd+y0, width, height);
		if(ret!=null)
		{
			ret.setX(xadd);
			ret.setY(yadd+y0);
			ret.setWidth(width);
			ret.setHeight(height);
		}
		return ret;
	}
	public static void applyBlendFunc(EBlendFunc blendFunc) {
		switch (blendFunc) {
		case off:
			GL11.glDisable(GL11.GL_BLEND);
			break;
		case SRC_ALPHA__ONE_MINUS_SRC_ALPHA:
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA:
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			
			glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
					GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA_PREMULTIPLIED:
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			glBlendFuncSeparate(GL11.GL_ONE,
					GL11.GL_ONE_MINUS_SRC_ALPHA,
					GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;

		default:
			throw new RuntimeException("Blend constant not implemented: "+blendFunc);
		}
	}
	/**
	 * On some platforms glBlendFuncSeparate must be called as "ext" feature
	 * @param sfactorRGB
	 * @param dfactorRGB
	 * @param sfactorAlpha
	 * @param dfactorAlpha
	 */
	static private final void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB,
			int sfactorAlpha, int dfactorAlpha) {
		GL14.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
	}
}
