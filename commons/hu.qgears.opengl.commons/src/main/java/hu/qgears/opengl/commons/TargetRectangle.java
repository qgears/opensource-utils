package hu.qgears.opengl.commons;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Class to define the target rectangle of a (sprite like)
 * texture to draw to.
 * @author rizsi
 *
 */
public class TargetRectangle {
	private Vector3f topLeft, topRight, bottomLeft, bottomRight;
	
	public TargetRectangle(Vector3f topLeft, Vector3f topRight,
			Vector3f bottomLeft, Vector3f bottomRight) {
		super();
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomLeft = bottomLeft;
		this.bottomRight = bottomRight;
	}

	public Vector3f getTopLeft() {
		return topLeft;
	}

	public Vector3f getTopRight() {
		return topRight;
	}

	public Vector3f getBottomLeft() {
		return bottomLeft;
	}

	public Vector3f getBottomRight() {
		return bottomRight;
	}
	/**
	 * Create a rectangle in the OpenGL coordinate system.
	 * TODO document pixels included and not included
	 */
	public TargetRectangle(Vector2f topLeft, Vector2f bottomRight) {
		init(topLeft, bottomRight);
	}
	/**
	 * Create a rectangle in the OpenGL coordinate system.
	 * TODO document pixels included and not included
	 */
	public TargetRectangle(float x0, float y0, float x1, float y1) {
		init(new Vector2f(x0,y0), new Vector2f(x1,y1));
	}
	/**
	 * Create a rectangle in the OpenGL coordinate system.
	 * TODO document pixels included and not included
	 */
	public TargetRectangle(Vector3f topLeft, Vector3f bottomRight) {
		init(new Vector2f(topLeft.x, topLeft.y),
				new Vector2f(bottomRight.x, bottomRight.y));
	}
	private void init(Vector2f topLeft, Vector2f bottomRight) {
		this.topLeft=new Vector3f(topLeft.x, topLeft.y, 0);
		this.bottomRight=new Vector3f(bottomRight.x, bottomRight.y, 0);
		this.topRight=new Vector3f();
		this.bottomLeft=new Vector3f();
		this.topRight.x=bottomRight.x;
		this.topRight.y=topLeft.y;
		this.bottomLeft.x=topLeft.x;
		this.bottomLeft.y=bottomRight.y;
	}
	
	public float getLeft()
	{
		return topLeft.getX();
	}
	public float getTop() {
		return topLeft.getY();
	}
	public float getBottom() {
		return bottomRight.getY();
	}
	public float getRight()
	{
		return bottomRight.getX();
	}

	public float getMiddleX()
	{
		return (getLeft()+getRight())/2;
	}
	public float getMiddleY()
	{
		return (getTopLeft().getY()+getBottomRight().getY())/2;
	}
	@Override
	public String toString() {
		return ""+getBottomLeft()+" "+getTopRight();
	}
	/**
	 * Create a target rectangle.
	 * TODO document pixels included and pixels not included.
	 * @param topLeft
	 * @param size
	 * @return
	 */
	public static TargetRectangle createTargetRectangleLeftTopAndSize(
			Vector2f topLeft,
			Vector2f size)
	{
		return createTargetRectangleLeftTopAndSize(topLeft, size, new Vector2f(1,1));
	}
	/**
	 * Create a target rectangle.
	 * TODO document pixels included and pixels not included.
	 * @param topLeft
	 * @param size
	 * @return
	 */
	public static TargetRectangle createTargetRectangleLeftTopAndSize(
			Vector3f topLeft,
			Vector3f size)
	{
		return createTargetRectangleLeftTopAndSize(topLeft, size, new Vector3f(1,1,0));
	}
	/**
	 * Create a target rectangle.
	 * TODO document pixels included and pixels not included.
	 * @param topLeft
	 * @param size
	 * @return
	 */
	public static TargetRectangle createTargetRectangleLeftTopAndSize(
			Vector2f topLeft,
			Vector2f size, Vector2f pixelSize)
	{
		return new TargetRectangle(topLeft,
				UtilGl.add(UtilGl.add(topLeft, size), UtilGl.mul(pixelSize, -1)));
	}
	/**
	 * Create a target rectangle.
	 * TODO document pixels included and pixels not included.
	 * @param topLeft
	 * @param size
	 * @return
	 */
	public static TargetRectangle createTargetRectangleLeftTopAndSize(
			Vector3f topLeft,
			Vector3f size, Vector3f pixelSize)
	{
		return new TargetRectangle(topLeft,
				UtilGl.add(UtilGl.add(topLeft, size), UtilGl.mul(pixelSize, -1)));
	}
	public static TargetRectangle createOne()
	{
		return new TargetRectangle(0,0,1,1);
	}
	public boolean contains(Vector2f point)
	{
		return point.x>=topLeft.x&&point.y>=topLeft.y&&
			point.y<=bottomRight.y&&point.x<=bottomRight.x;
	}

	public float getWidth() {
		return Math.abs(bottomLeft.x-bottomRight.x);
	}
	public float getHeight() {
		return Math.abs(topLeft.y-bottomRight.y);
	}
}
