package hu.qgears.opengl.commons;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a surface. For each (u,v) tessellation point specifies an (x,y,z) coordinate, that is on the surface. 
 * 
 * @author agostoni
 *
 */
public interface ITessellatedSurface {

	
	/**
	 * Returns the height of the (x,y) point.
	 * 
	 * @param u between 0 and 1, represent a tessellation point on x axle.
	 * @param y between 0 and 1, represent a tessellation point on y axle.
	 * @return
	 */
	Vector3f getPointAt (float u, float v);
	
	/**
	 * Returns, how many pieces the surface will be slip horizontally during tessellation.
	 * 
	 * @return
	 */
	public int getTesselU();
	
	/**
	 * Returns, how many pieces the surface will be slip vertically during tessellation.
	 * 
	 * @return
	 */
	public int getTesselV();
}
