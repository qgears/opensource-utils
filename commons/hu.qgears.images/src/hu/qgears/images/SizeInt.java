package hu.qgears.images;

/**
 * Wrapper of width and height pairs.
 * @author rizsi
 *
 */
public class SizeInt {
	private int width, height;

	public SizeInt(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	public int getArea()
	{
		return width*height;
	}
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
}
