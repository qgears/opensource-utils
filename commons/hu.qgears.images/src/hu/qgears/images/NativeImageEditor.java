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
	 * Replaces the pixels within specified rectangular area with black pixels
	 * (rgb(0,0,0)).
	 * 
	 * @param topLeftX
	 * @param topLeftY
	 * @param width
	 * @param height
	 */
	public NativeImageEditor applyDontcareRegion(int topLeftX, int topLeftY, int width, int height){
		byte zByte = toByte(0);
		doFill(zByte , zByte, zByte, zByte, topLeftX, topLeftY, width, height);
		return this;
	}
	
	
	/**
	 * Fills all pixels of image with specified color.
	 * 
	 * @param backgroundColor
	 * @return
	 * @since 3.0
	 */
	public NativeImageEditor fillWithColor(RGBAColor backgroundColor){
		ENativeImageComponentOrder co = imageToEdit.getComponentOrder();
		switch (co) {
		case BGR :
		case BGRA: {
			doFill(toByte(backgroundColor.b), toByte(backgroundColor.g),
					toByte(backgroundColor.r), toByte(backgroundColor.a));
			break;
		}
		case RGB :
		case RGBA: {
			doFill(toByte(backgroundColor.r), toByte(backgroundColor.g),
					toByte(backgroundColor.b), toByte(backgroundColor.a));
			break;
		}
		case ABGR: {
			doFill(toByte(backgroundColor.a), toByte(backgroundColor.b),
					toByte(backgroundColor.g), toByte(backgroundColor.r));
			break;
		}
		case ARGB: {
			doFill(toByte(backgroundColor.a), toByte(backgroundColor.r),
					toByte(backgroundColor.g), toByte(backgroundColor.b));
			break;
		}
		case ALPHA: {
			imageToEdit.clearAlpha((byte) backgroundColor.a);
			break;
		}
		case MONO:{
			for (int i = 0; i < imageToEdit.getWidth(); i++) {
				for (int j = 0; j < imageToEdit.getHeight(); j++) {
					imageToEdit.setChannel(i, j, 0, toByte (backgroundColor.a));
				}
			}
			break;
		}
		default:
			throw new RuntimeException("background color fill is not supported for this pixel order :"+co);
		}
		return this;
	}
	
	private byte toByte(int i){
		return (byte) (i & 0xFF);
	}
	
	private void doFill(byte first, byte second, byte third, byte fourth){
		doFill(first, second, third, fourth,0,0,imageToEdit.getWidth(), imageToEdit.getHeight());
	}
	
	private void doFill(byte first, byte second, byte third, byte fourth, int x, int y, int width, int height){
		for (int i = x; i< x+width; i++){
			for(int j = y; j< y+height; j++){
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
	 * Returns the underlying image, that is edited using this editor.
	 * 
	 * @return
	 */
	public NativeImage getImageToEdit() {
		return imageToEdit;
	}
}
