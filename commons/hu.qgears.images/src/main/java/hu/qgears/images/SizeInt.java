package hu.qgears.images;

/**
 * Wrapper of width and height pairs.
 * @author rizsi
 *
 */
public class SizeInt {
	private int width, height;

	/**
	 * Construct an immutable {@link SizeInt} that represents the width-height
	 * pair of an element.
	 * 
	 * @param width
	 * @param height
	 */
	public SizeInt(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	/**
	 * @return the width as passed in the constructor
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height as passed in the constructor
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Calculates the area of the element represented by this {@link SizeInt}
	 * object, and returns the number of pixels (width*height).
	 * 
	 * @return the number of pixels (width * height)
	 */
	public int getNumberOfPixels()
	{
		return width*height;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SizeInt)
		{
			SizeInt other=(SizeInt) obj;
			return other.width==width&&other.height==height;
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return "["+width+"x"+height+"]";
	}
	@Override
	public int hashCode() {
		return height+2048*width;
	}
}
