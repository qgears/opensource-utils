package hu.qgears.images;

import java.nio.ByteBuffer;

import hu.qgears.images.text.RGBAColor;

/**
 * Utility methods that support editing a native image from Java code. Basic
 * image manipulation utilities are supported, like filling image with colors,
 * or overwrite pixels of a rectangular area.
 * 
 * @author agostoni
 * 
 */
public class NativeImageEditor {

	private NativeImage imageToEdit;
	
	private int cropXmin, cropYmin, cropXmax, cropYmax;

	public NativeImageEditor(NativeImage imageToEdit) {
		super();
		this.imageToEdit = imageToEdit;
		if (imageToEdit == null){
			throw new IllegalArgumentException("imageToEdit is null");
		}
		cropXmax=imageToEdit.getWidth();
		cropYmax=imageToEdit.getHeight();
	}
	
	/**
	 * Fills a rectangle with given color data. If the
	 * {@link NativeImage#getComponentOrder() component order / type} of the
	 * edited image is {@link ENativeImageComponentOrder#MONO MONO}, only the 
	 * {@link RGBAColor#a alpha component} will be used as the color; the rest 
	 * of the color components will be discarded. The only formats supported by 
	 * this method are: {@link ENativeImageComponentOrder#RGB RGB},
	 * {@link ENativeImageComponentOrder#RGBA RGBA} and 
	 * {@link ENativeImageComponentOrder#MONO MONO} - note that no type checking
	 * will be performed!
	 * @param topLeftX the left coordinate of the rectangle to fill
	 * @param topLeftY the top coordinate of the rectangle to fill
	 * @param width the width of the rectangle to fill
	 * @param height the height of the rectangle to fill
	 * @return this editor for method chaining
	 * @since 6.2
	 */
	public NativeImageEditor fillRect(int topLeftX, int topLeftY,
			int width, int height, final RGBAColor color) {
		width=cropWidth(topLeftX, width, 0, imageToEdit.getWidth());
		topLeftX=cropCoordinate(topLeftX, 0, imageToEdit.getWidth());
		height=cropWidth(topLeftY, height, 0, imageToEdit.getHeight());
		topLeftY=cropCoordinate(topLeftY, 0, imageToEdit.getHeight());
		if(width<1)
		{
			return this;
		}
		if(height<1)
		{
			return this;
		}
		if(topLeftX>=imageToEdit.getWidth()||topLeftX<0)
		{
			return this;
		}
		if(topLeftY>=imageToEdit.getHeight()||topLeftY<0)
		{
			return this;
		}
		switch (imageToEdit.getComponentOrder()) {
		case MONO: {
			doFillMono(topLeftX, topLeftY, width, height, toByte(color.a));
			break;
		}
		case RGB:
		case BGRA:
		case RGBA:
		case ABGR:
		case ARGB:
		case BGR:
		{
			byte[] pattern=imageToEdit.getComponentOrder().getPixelFormat().colorToPattern(color);
			doFillGeneric(pattern, topLeftX, topLeftY, width, height);
			break;
		}
		default:
			throw new RuntimeException("color fill is not supported for this "
					+ "pixel order :" + imageToEdit.getComponentOrder());
		}

		return this;
	}
	
	/**
	 * Draws a point to the given coordinate
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the RGB color of the point
	 * @return
	 */
	public NativeImageEditor drawPoint(final int x, final int y, final RGBAColor color) {
		if(x<cropXmin||x>=cropXmax)
		{
			return this;
		}
		if(y<cropYmin||y>=cropYmax)
		{
			return this;
		}
		switch (imageToEdit.getComponentOrder()) {
		case RGBA:
		{
			imageToEdit.setChannel(x, y, 0, toByte(color.r));
			imageToEdit.setChannel(x, y, 1, toByte(color.g));
			imageToEdit.setChannel(x, y, 2, toByte(color.b));
			if (imageToEdit.getComponentOrder().getAlphaChannel() >= 0){
				imageToEdit.setChannel(x, y, 3, toByte(color.a));
			}
			break;
		}
		case MONO:
		{
			imageToEdit.setChannel(x, y, 0, toByte((color.r+color.g+color.b)/3));
			break;
		}
		default:
			imageToEdit.setPixel(x, y, color);
			break;
		}
		return this;
	}
	
	/**
	 * Draws a vertical line
	 * @param topX
	 * @param topY
	 * @param height
	 * @param color
	 * @param dashed
	 * @return
	 */
	public NativeImageEditor drawLineVertical (final int topX, final int topY, 
			final int height, final RGBAColor color, boolean dashed) {
		if(topX<0||topX>=imageToEdit.getWidth())
		{
			return this;
		}
		for (int j = topY; j < topY + height; j++) {
			if (dashed) {
				if ((j-topY) % 15 > 10) {
					continue;
				}
			}
			drawPoint(topX, j, color);
		}
		return this;
	}
	
	/**
	 * Draws a horizontal line
	 * @param leftX
	 * @param leftY
	 * @param width
	 * @param color
	 * @param dashed
	 * @return
	 */
	public NativeImageEditor drawLineHorizontal (final int leftX, final int leftY, 
			final int width, final RGBAColor color, boolean dashed) {
		for (int i = leftX; i < leftX + width; i++) {
			if (dashed) {
				if ((i-leftX) % 15 > 10) {
					continue;
				}
			}
			drawPoint(i, leftY, color);
		}
		return this;
	}
	
	/**
	 * Draws a line inside the image
	 * @param X0
	 * @param Y0
	 * @param X1
	 * @param Y1
	 * @param width
	 * @param color
	 * @return
	 */
	public NativeImageEditor drawLine (final int X0, final int Y0, final int X1, final int Y1,
			final int width, final RGBAColor color) {
		if (Math.abs(X1-X0) >= Math.abs(Y1-Y0)) {
			if (Math.abs(X1-X0) < 1) { // Just one point
				return this;
			}
			int xStep = (X1-X0) / Math.abs(X1-X0);
			double m = ((double)(Y1-Y0)) / ((double)(X1-X0));
			for (int x = 0; Math.abs(x) <= Math.abs(X1-X0); x=x+xStep) {
				for (int i = 0; i < width; i++) {
					int yMod = (i+1)/2 * (i%2==0 ? 1 : -1);
					int yCoord = Y0+(int)Math.floor(m*x) + yMod;
					if (yCoord >=0 && yCoord < imageToEdit.getHeight()) {
						drawPoint(X0+x, yCoord, color);
					}
				}
			}
		} else {
			int yStep = (Y1-Y0) / Math.abs(Y1-Y0);
			double m = ((double)(X1-X0)) / ((double)(Y1-Y0));
			for (int y = 0; Math.abs(y) <= Math.abs(Y1-Y0); y=y+yStep) {
				for (int i = 0; i < width; i++) {
					int xMod = (i+1)/2 * (i%2==0 ? 1 : -1);
					int xCoord = X0+(int)Math.floor(m*y) + xMod;
					if (xCoord >=0 && xCoord < imageToEdit.getWidth()) {
						drawPoint(xCoord, Y0+y, color);
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * Draws a filled circle
	 * @param xc central point X coordinate
	 * @param yc central point Y coordinate
	 * @param r radius in pixels
	 * @param col the color of the circle
	 */
	public void drawFilledCircle(int xc, int yc, int r, RGBAColor col) {
		double r2 = (r+0.5)*(r+0.5);
		double distance2;
		for (int x = xc -r; x < xc + r + 1; x++) {
			for (int y = yc - r; y < yc + r + 1; y++) {
				distance2 = (double)((xc-x)*(xc-x) + (yc-y)*(yc-y));
				if (distance2 < r2) {
					drawPoint(x, y, col);
				}
			}
		}
	}
	
	/**
	 * Draws a circle with given width
	 * @param xc central point X coordinate
	 * @param yc central point Y coordinate
	 * @param r radius in pixels
	 * @param width circle width in pixels
	 * @param col the color of the circle
	 */
	public void drawCircle(int xc, int yc, int r, int width, RGBAColor col) {
		double r2Max = (r+0.5)*(r+0.5);
		double r2Min = (r+0.5-width)*(r+0.5-width);
		double distance2;
		for (int x = xc -r; x < xc + r + 1; x++) {
			for (int y = yc - r; y < yc + r + 1; y++) {
				distance2 = (double)((xc-x)*(xc-x) + (yc-y)*(yc-y));
				if (distance2 < r2Max && distance2 > r2Min) {
					drawPoint(x, y, col);
				}
			}
		}
	}
	  
	/**
	 * Replaces the pixels within specified rectangular area with black pixels
	 * (rgb(0,0,0)). If an alpha channel is present, the relevant pixels will be
	 * filled with zeros, too. {@link ENativeImageComponentOrder#MONO MONO} 
	 * images are not supported by this method!
	 * 
	 * @param topLeftX the leftmost coordinate of the rectangle to be filled
	 * @param topLeftY the topmost coordinate of the rectangle to be filled
	 * @param width the width of the rectangle to be filled
	 * @param height the height of the rectangle to be filled
	 * @return this editor for method chaining
	 */
	public NativeImageEditor applyDontcareRegion(int topLeftX, int topLeftY, 
			int width, int height) {
		fillRect(topLeftX, topLeftY, width, height, new RGBAColor(0, 0, 0, 0));
		
		return this;
	}
	
	/**
	 * Fills all pixels of image with specified color.
	 * 
	 * @param color
	 * @return this editor for method chaining
	 * @since 3.0
	 */
	public NativeImageEditor fillWithColor(final RGBAColor color) {
		final ENativeImageComponentOrder co = imageToEdit.getComponentOrder();
		
		switch (co) {
		case BGR :
		case BGRA:
		case RGB :
		case RGBA:
		case ABGR:
		case ARGB:
			byte[] pattern=co.getPixelFormat().colorToPattern(color);
			fillPattern(pattern);
			break;
		case ALPHA: {
			imageToEdit.clearAlpha((byte) color.a);
			break;
		}
		case MONO:{
			doFillMono(0, 0, imageToEdit.getWidth(), imageToEdit.getHeight(), 
					toByte(color.a));
			break;
		}
		default:
			throw new RuntimeException("background color fill is not supported "
					+ "for this pixel order :"+co);
		}
		return this;
	}
	
	private void fillPattern(byte[] pattern) {
		ByteBuffer bb=imageToEdit.getBuffer().getJavaAccessor().duplicate();
		int step=imageToEdit.getStep();
		int w=imageToEdit.getWidth();
		int h=imageToEdit.getHeight();
		for(int y=0;y<h;++y)
		{
			bb.position(step*y);
			for(int x=0;x<w;++x)
			{
				bb.put(pattern);
			}
		}
	}

	private byte toByte(int i){
		return (byte) (i & 0xFF);
	}

	/**
	 * Fills a rectangle with given color data. Note that the names of 
	 * parameters called '{@code first}', '{@code second}', '{@code third}', 
	 * and '{@code fourth}' refer to the in-pixel offset of a color byte. Note 
	 * that {@link ENativeImageComponentOrder#MONO MONO} images are not 
	 * supported by this method.
	 *  
	 * @param pattern
	 * @param x the left coordinate of the rectangle to fill
	 * @param y the top coordinate of the rectangle to fill
	 * @param width the width of the rectangle to fill
	 * @param height the height of the rectangle to fill
	 */
	private void doFillGeneric(byte[] pattern, //NOSONAR
			int x, int y, int width, int height) { // NOSONAR
		int step=imageToEdit.getStep();
		int bytePerPixel=imageToEdit.getComponentOrder().getPixelFormat().getBytesPerPixel();
		ByteBuffer bb=imageToEdit.getBuffer().getJavaAccessor().duplicate();
		for (int j = y; j < y + height; j++) {
			bb.position(j*step+x*bytePerPixel);
			for (int i = x; i < x + width; i++) {
				bb.put(pattern);
			}
		}
	}
	
	/**
	 * Fills a rectangle with given color data. Note that only 
	 * {@link ENativeImageComponentOrder#MONO MONO} images are  supported by 
	 * this method.
	 *  
	 * @param x the left coordinate of the rectangle to fill
	 * @param y the top coordinate of the rectangle to fill
	 * @param width the width of the rectangle to fill
	 * @param height the height of the rectangle to fill
	 * @param color the color value, ranging from  
	 */
	private void doFillMono(int x, int y, int width, int height, byte color) {
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				imageToEdit.setChannel(i, j, 0, color);
			}
		}
	}
	
	/**
	 * Returns the underlying image, that is edited using this editor.
	 * 
	 * @return
	 */
	public NativeImage getImageToEdit() {
		return imageToEdit;
	}
	private int cropCoordinate(int coo, int min, int max) {
		if(coo>max)
		{
			coo=max;
		}
		if(coo<min)
		{
			coo=min;
		}
		return coo;
	}
	private int cropWidth(int coo, int width, int min, int max) {
		int coocropped=cropCoordinate(coo, min, max);
		int diff=coocropped-coo;
		width-=diff;
		int overflow=coocropped+width-max;
		if(overflow>0)
		{
			width-=overflow;
		}
		return width;
	}
	public NativeImageEditor setCropRectangle(int xMin, int xMax, int yMin, int yMax)
	{
		cropXmin=xMin;
		cropXmax=xMax;
		cropYmin=yMin;
		cropYmax=yMax;
		return this;
	}
}
