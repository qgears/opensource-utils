package hu.qgears.images.text;

import java.util.Locale;

import hu.qgears.commons.UtilString;
import hu.qgears.images.NativeImage;

/**
 * This class represents a color defined by r,g,b,a color components (each
 * component is represented by a 0-255 integer value).
 * <p>
 * Defines conversion methods for getting multiple representation of the same
 * color (CSS, hexa, 0..1 floating point coordinates).
 * <p>
 * Also defines constants for the most frequently used colors.
 * 
 * @author agostoni
 * @since 3.0
 */
public class RGBAColor {
	private static final String hahstagColorFormat = "#%02x%02x%02x";

	/**
	 * Color constant of white.
	 */
	public static final RGBAColor WHITE = new RGBAColor(255, 255, 255, 255);

	/**
	 * Color constant of black.
	 */
	public static final RGBAColor BLACK = new RGBAColor(0, 0, 0, 255);

	/**
	 * Color constant of black.
	 */
	public static final RGBAColor BLUE = new RGBAColor(0, 0, 255,255);
	/**
	 * Color constant of red.
	 */
	public static final RGBAColor RED = new RGBAColor(255, 0, 0,255);

	/**
	 * Color constant of yellow.
	 */
	public static final RGBAColor YELLOW = new RGBAColor(255, 255, 0,255);

	/**
	 * Color constant of green.
	 */
	public static final RGBAColor GREEN = new RGBAColor(0, 255, 0,255);
	/**
	 * Color constant of purple.
	 */
	public static final RGBAColor PURPLE = new RGBAColor(255, 0, 255,255);
	/**
	 * Color constant of anthracite gray.
	 */
	public static final RGBAColor ANTHRACITE = new RGBAColor(30,30,30,255);
	
	
	private static final float TO_FLOAT_MULTIPLIER =  1.0f / 255.0f;

	/**
	 * The red component of the color in 0-255 range.
	 */
	public final int r;
	/**
	 * The green component of the color in 0-255 range.
	 */
	public final int g;
	/**
	 * The blue component of the color in 0-255 range.
	 */
	public final int b;
	/**
	 * The alpha component of the color in 0-255 range. 0 means full
	 * transparent, while 255 means full opaque color.
	 */
	public final int a;

	/**
	 * Creates a new color instance with specified color components.
	 * 
	 * @param r see {@link #r}
	 * @param g see {@link #g}
	 * @param b see {@link #b}
	 * @param a see {@link #a}
	 * 
	 * @throws IllegalArgumentException if color component values are out of range [0,255]
	 */
	public RGBAColor(int r, int g, int b, int a) {
		super();
		assertValidInt(r);
		assertValidInt(g);
		assertValidInt(b);
		assertValidInt(a);
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	/**
	 * Creates a new opaque color instance with specified color components.
	 * 
	 * @param r see {@link #r}
	 * @param g see {@link #g}
	 * @param b see {@link #b}
	 * 
	 * @throws IllegalArgumentException if color component values are out of range [0,255]
	 */
	public RGBAColor(int r, int g, int b) {
		this(r,g,b,255);
	}
	
	/**
	 * Utility for crating a new color from float values.
	 * 
	 * @param r_f
	 *            The red component of the color in 0.0-1.0 range.
	 * @param g_f
	 *            The green component of the color in 0.0-1.0 range.
	 * @param b_f
	 *            The blue component of the color in 0.0-1.0 range.
	 * @param a_f
	 *            The alpha component of the color in 0.0-1.0 range. 0.0 means
	 *            full transparent, while 1.0 means full opaque color.
	 * @return
	 * @throws IllegalArgumentException if color components are out of range.
	 */
	public static final RGBAColor fromFloats(float r_f,float g_f,float b_f,float a_f) {
		int r = toInt(r_f);
		int g = toInt(g_f);
		int b = toInt(b_f);
		int a = toInt(a_f);
		return new RGBAColor(r, g, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RGBAColor)
		{
			RGBAColor other=(RGBAColor) obj;
			return other.r==r&&other.g==g&&other.b==b&&other.a==a;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return r^(g<<8)^(b<<16)^(a<<24);
	}

	/**
	 * Converts this color to 'rgba(r,g,b,a)' format (css notation). If a ==
	 * 255, then the simplified 'rgb(r,g,b)' format will be used.
	 * 
	 * @return
	 */
	public String toCssParameter(){
		if(a==255) {
			return "rgb("+ r+","+g+","+b+")";
		} else {
			return "rgba("+r+","+g+","+b+","+(TO_FLOAT_MULTIPLIER * a)+")";
		}
	}
	/**
	 * Converts this color to '#DEADBE' format (css notation).
	 * @return
	 */
	public String toHashTagCssNotation(){
		String s = String.format((Locale) null, hahstagColorFormat, r,
				g, b);
		return s;
	}
	
	/**
	 * Converts this DTO to float vector representation. The length of returned
	 * array is always 4, and contains the values of red, green ,blue and alpha
	 * channels (in this order).
	 * <p>
	 * The channel values will be in range [0f .. 1f].
	 * 
	 * @return
	 * @see #updateFromFloatVector(float, float, float, float)
	 */
	public float [] toFloatVector(){
		float[] vector = new float[4];
		vector[0] = ((float)r) * TO_FLOAT_MULTIPLIER;
		vector[1] = ((float)g) * TO_FLOAT_MULTIPLIER;
		vector[2] = ((float)b) * TO_FLOAT_MULTIPLIER;
		vector[3] = ((float)a) * TO_FLOAT_MULTIPLIER;
		return vector;
	}

	private static void assertValidFloat(float a_f) {
		if (a_f < 0f || a_f > 1f){
			throw new IllegalArgumentException("Color coordinate is out of range [0,1] : "+a_f);
		}
	}
	private static void assertValidFloat(double a_f) {
		if (a_f < 0f || a_f > 1f){
			throw new IllegalArgumentException("Color coordinate is out of range [0,1] : "+a_f);
		}
	}
	private static void assertValidInt(int c) {
		if (c < 0 || c > 255){
			throw new IllegalArgumentException("Color coordinate is out of range [0,255] : "+c);
		}
	}
	
	/**
	 * Scales 0..1 floating point color coordinate to 0..255 integer coordinate.
	 * 
	 * 
	 * @param parseFloat The float color coordinate to parse as int.
	 * @return
	 * @throws IllegalArgumentException if specified float value is not between 0..1
	 * 
	 */
	public static int toInt(float parseFloat) {
		assertValidFloat(parseFloat);
		return Math.round((parseFloat / TO_FLOAT_MULTIPLIER));
	}
	/**
	 * Scales 0..1 double floating point color coordinate to 0..255 integer coordinate.
	 * 
	 * 
	 * @param parseFloat The double color coordinate to parse as int.
	 * @return
	 * @throws IllegalArgumentException if specified float value is not between 0..1
	 * 
	 */
	public static int toInt(double parseFloat) {
		assertValidFloat(parseFloat);
		return (int)Math.round((parseFloat / TO_FLOAT_MULTIPLIER));
	}
	/**
	 * Parse a CSS notation color string. Handles rgb(...), rgba(...) and #... formats
	 * The #... format does not handle alpha channel, only r g and b.
	 * @param cssNotation the CSS string notation of the color
	 * @param defaultRet this color is returned in case parsing fails
	 * @return the color parsed as a {@link WColor} object or the default value in case
	 *  of parse error.
	 *  
	 */
	public static RGBAColor fromCssNotation(String cssNotation, RGBAColor defaultRet) {
		try
		{
			{
				String prefix = "rgb(";
				if (cssNotation.startsWith(prefix)) {
					String s = cssNotation.substring(cssNotation
							.lastIndexOf(prefix) + 4);
					s = s.substring(0, s.length() - 1);
					String[] nums = s.split(",");
					// rgb(1,2,3) -> 1,2,3) -> 1,2,3 -> [1,2,3]
					return new RGBAColor(Integer.parseInt(nums[0].trim()),
							Integer.parseInt(nums[1].trim()),
							Integer.parseInt(nums[2].trim()), 255);
				}
			}
			{
				String prefix = "rgba(";
				if (cssNotation.startsWith(prefix)) {
					String s = cssNotation.substring(cssNotation
							.lastIndexOf(prefix) + 5);
					s = s.substring(0, s.length() - 1);
					String[] nums = s.split(",");
					// rgb(1,2,3) -> 1,2,3) -> 1,2,3 -> [1,2,3]
					return new RGBAColor(Integer.parseInt(nums[0].trim()),
							Integer.parseInt(nums[1].trim()),
							Integer.parseInt(nums[2].trim()),
							toInt(Float.parseFloat(nums[3].trim())));
				}
			}
			{
				String prefix="#";
				if (cssNotation.startsWith(prefix)) {
					String s = cssNotation.substring(1);
					// rgb(1,2,3) -> 1,2,3) -> 1,2,3 -> [1,2,3]
					return new RGBAColor(
							Integer.parseInt(s.substring(0,2), 16),
							Integer.parseInt(s.substring(2,4), 16),
							Integer.parseInt(s.substring(4,6), 16),
							255);
				}
			}
		}catch(Exception e) {} // NOSONAR We don't log errors here
		return defaultRet;
	}
	
	@Override
	public String toString() {
		return toCssParameter();
	}
	
	/**
	 * Converts this color to a hexadecimal representation, without the '0x' prefix.
	 * <p>
	 * Example : rgba(255,0,255,0) -> FF00FF00
	 * 
	 * @return
	 */
	public String toHexRepresentation() {
		return ""+toHex(r)+toHex(g)+toHex(b)+toHex(a);
	}
	
	private String toHex(int v) {
		return UtilString.padLeft(Integer.toHexString(v), 2, '0');
	}
	/**
	 * Creates a new text with same color coordinates as this object, but with a different alpha value.
	 * 
	 * @param newAlpha The new alpha value. Must be in range [0;255]
	 * @return
	 */
	public RGBAColor newWithAlpha(int newAlpha) {
		assertValidInt(newAlpha);
		return new RGBAColor(r, g, b,newAlpha);
	}
	/**
	 * The same 32 bit representation as {@link NativeImage} getPixel and setPixel
	 * @return
	 */
	public int toIntPixel() {
		return ((r<<24)&0xFF000000)+((g<<16)&0x00FF0000)+((b<<8)&0x0000FF00)+(a&0xFF);
	}
	/**
	 * Parse the same 32 bit representation as {@link NativeImage} getPixel and setPixel
	 * @return
	 */
	public static RGBAColor fromIntPixel(int value) {
		return new RGBAColor((value>>24)&0xff, (value>>16)&0xff,(value>>8)&0xff,value&0xff);
	}
}
