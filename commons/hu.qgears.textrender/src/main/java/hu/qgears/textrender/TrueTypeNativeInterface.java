package hu.qgears.textrender;

import java.nio.ByteBuffer;

import hu.qgears.images.SizeInt;
import hu.qgears.images.text.EHorizontalAlign;
import hu.qgears.images.text.EVerticalAlign;
import hu.qgears.images.text.EWrapMode;

public interface TrueTypeNativeInterface {
	/**
	 * Creates a render surface. Assumptions :
	 * 
	 * * RGBA pixel representation. * Size of data equals w * h * 4
	 * 
	 * @param data
	 * @param w
	 * @param h
	 * 
	 *             Returns the surface id (handle) that identifies this instance.
	 */
	long createSurfaceWithData(ByteBuffer data, int w, int h);
	
	void disposeSurface(long surfaceHandle);
	/**
	 * 
	 * @param surfaceHandle the surface handle returned by {@link #createSurfaceWithData(ByteBuffer, int, int)}. Might be zero, if parameter render = false.
	 * @param fontFamily
	 * @param text
	 * @param hAlign
	 * @param vAlign
	 * @param x X coord of top left corner
	 * @param y Y coord of top left corner
	 * @param width The maximal width of the bonding box within surface.
	 * @param height The maximal height of the bonding box within surface.
	 * @param r color
	 * @param g
	 * @param b
	 * @param a alpha channel of the color
	 * @param clip clip text if does not fit into specified area
	 * @param wrapMode How to wrap long texts amongst white spaces
	 * @return The bounding box calculated during laying out the text 
	 */
	SizeInt renderText(
			long surfaceHandle,
			String fontFamily, 
			String text, 
			EHorizontalAlign hAlign, 
			EVerticalAlign vAlign, 
			int x, int y, int width, int height, 
			float r, float g, float b, float a, 
			boolean clip, EWrapMode wrapMode);
	/**
	 * 
	 * @param fontFamily
	 * @param text
	 * @param hAlign
	 * @param vAlign
	 * @param width The maximal width of the bonding box within surface.
	 * @param height The maximal height of the bonding box within surface.
	 * @param wrapMode How to wrap long texts amongst white spaces
	 * @return The bounding box calculated during laying out the text 
	 */
	SizeInt layoutText(
			String fontFamily, 
			String text, 
			EHorizontalAlign hAlign, 
			EVerticalAlign vAlign, 
			int width, int height, 
			EWrapMode wrapMode);
}
