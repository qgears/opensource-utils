package hu.qgears.images;

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

	public NativeImageEditor(NativeImage imageToEdit) {
		super();
		this.imageToEdit = imageToEdit;
		if (imageToEdit == null){
			throw new IllegalArgumentException("imageToEdit is null");
		}
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
	public NativeImageEditor fillRect(final int topLeftX, final int topLeftY,
			final int width, final int height, final RGBAColor color) {
		switch (imageToEdit.getComponentOrder()) {
		case MONO: {
			doFillMono(topLeftX, topLeftY, width, height, toByte(color.a));
			break;
		}
		case RGB:
		case RGBA: {
			doFillGeneric(toByte(color.r), toByte(color.g), toByte(color.b), 
					toByte(color.a), topLeftX, topLeftY, width, height);
		}
		default:
			throw new RuntimeException("color fill is not supported for this "
					+ "pixel order :" + imageToEdit.getComponentOrder());
		}

		return this;
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
		byte zByte = toByte(0);
		
		doFillGeneric(zByte, zByte, zByte, zByte, topLeftX, topLeftY, width, height);
		
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
		case BGRA: {
			doFill(toByte(color.b), toByte(color.g),
					toByte(color.r), toByte(color.a));
			break;
		}
		case RGB :
		case RGBA: {
			doFill(toByte(color.r), toByte(color.g),
					toByte(color.b), toByte(color.a));
			break;
		}
		case ABGR: {
			doFill(toByte(color.a), toByte(color.b),
					toByte(color.g), toByte(color.r));
			break;
		}
		case ARGB: {
			doFill(toByte(color.a), toByte(color.r),
					toByte(color.g), toByte(color.b));
			break;
		}
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
	
	private byte toByte(int i){
		return (byte) (i & 0xFF);
	}
	
	private void doFill(byte first, byte second, byte third, byte fourth) {
		doFillGeneric(first, second, third, fourth, 
				0, 0, imageToEdit.getWidth(), imageToEdit.getHeight());
	}
	
	/**
	 * Fills a rectangle with given color data. Note that the names of 
	 * parameters called '{@code first}', '{@code second}', '{@code third}', 
	 * and '{@code fourth}' refer to the in-pixel offset of a color byte. Note 
	 * that {@link ENativeImageComponentOrder#MONO MONO} images are not 
	 * supported by this method.
	 *  
	 * @param first the color byte at pixel offset + 0
	 * @param second the color byte at pixel offset + 1
	 * @param third the color byte at pixel offset + 2
	 * @param fourth the color byte at pixel offset + 3
	 * @param x the left coordinate of the rectangle to fill
	 * @param y the top coordinate of the rectangle to fill
	 * @param width the width of the rectangle to fill
	 * @param height the height of the rectangle to fill
	 */
	/* too many parameters are OK here */
	private void doFillGeneric(byte first, byte second, byte third, byte fourth,
			int x, int y, int width, int height) { // NOSONAR
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				imageToEdit.setChannel(i, j, 0, first);
				imageToEdit.setChannel(i, j, 1, second);
				imageToEdit.setChannel(i, j, 2, third);
				if (imageToEdit.getComponentOrder().getAlphaChannel() >= 0){
					imageToEdit.setChannel(i, j, 3, fourth);
				}
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
}
