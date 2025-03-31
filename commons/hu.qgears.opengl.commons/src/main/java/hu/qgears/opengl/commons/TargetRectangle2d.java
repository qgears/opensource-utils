package hu.qgears.opengl.commons;


public class TargetRectangle2d {

	/*
	 * Simple DTO class; providing getters and setters does not improve security
	 */
	@SuppressWarnings("squid:ClassVariableVisibilityCheck")
	public float x, y, right, bottom;

	public TargetRectangle2d(float x, float y, float right, float bottom) {
		super();
		this.x = x;
		this.y = y;
		this.right = right;
		this.bottom = bottom;
	}

	public float getMiddleY() {
		return (y+bottom)/2;
	}

	public float getMiddleX() {
		return (x+right)/2;
	}
	@Override
	public String toString() {
		return "["+x+","+y+":"+right+","+bottom+"]";
	}

	public TargetRectangle2d set(float x, float y, float right, float bottom) {
		this.x = x;
		this.y = y;
		this.right = right;
		this.bottom = bottom;
		return this;
	}
}
