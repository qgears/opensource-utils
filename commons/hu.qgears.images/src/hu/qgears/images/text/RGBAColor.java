package hu.qgears.images.text;

import hu.qgears.commons.UtilString;

/**
 * @since 3.0
 */
public class RGBAColor {
	public static final RGBAColor WHITE = new RGBAColor(255, 255, 255, 255);

	public static final RGBAColor BLACK = new RGBAColor(0, 0, 0, 255);

	public static final RGBAColor BLUE = new RGBAColor(0, 0, 255,255);
	public static final RGBAColor RED = new RGBAColor(255, 0, 0,255);

	public static final RGBAColor YELLOW = new RGBAColor(255, 255, 0,255);

	public static final RGBAColor GREEN = new RGBAColor(0, 255, 0,255);
	public static final RGBAColor PURPLE = new RGBAColor(255, 0, 255,255);
	public static final RGBAColor ANTHRACITE = new RGBAColor(30,30,30,255);
	
	
	private static final float TO_FLOAT_MULTIPLIER =  1.0f / 255.0f;

	public final int r, g, b, a;

	public RGBAColor(int r, int g, int b, int a) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	/**
	 * @since 3.0
	 */
	public RGBAColor(int r, int g, int b) {
		this(r,g,b,255);
	}
	
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
	 * Converts this color to 'rgba(r,g,b,a)' format. If a == 255, then the
	 * simplified 'rgb(r,g,b)' format will be used.
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
	 * Converts this DTO to float vector representation. The length of returned
	 * array is always 4, and contains the values of red, green ,blue and alpha
	 * channels (in this order).
	 * <p>
	 * The channel values will be in range [0f .. 1f], if the original DTO was
	 * valid (rgba values from 0 to 255).
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
//	/**
//	 * Updates this DTO based on specified float color coordinates.
//	 * 
//	 * @param r_f Value of red channel (0f .. 1f)
//	 * @param g_f Value of green channel (0f .. 1f)
//	 * @param b_f Value of blue channel (0f .. 1f)
//	 * @param a_f Value of alpha channel (0f .. 1f)
//	 * 
//	 * @throws IllegalArgumentException if one of the the specified values are out of range.
//	 * @see #toFloatVector()
//	 */
//	public void updateFromFloatVector(float r_f,float g_f,float b_f,float a_f){
//		assertValidFloat(r_f);
//		assertValidFloat(g_f);
//		assertValidFloat(b_f);
//		assertValidFloat(a_f);
//		this.r = Math.round(r_f / TO_FLOAT_MULTIPLIER);
//		this.g = Math.round(g_f / TO_FLOAT_MULTIPLIER);
//		this.b = Math.round(b_f / TO_FLOAT_MULTIPLIER);
//		this.a = Math.round(a_f / TO_FLOAT_MULTIPLIER);
//	}

	private static void assertValidFloat(float a_f) {
		if (a_f < 0f || a_f > 1f){
			throw new IllegalArgumentException("Color coordinate is out of range [0,1] : "+a_f);
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
		assertValidFloat(TO_FLOAT_MULTIPLIER * newAlpha);
		return new RGBAColor(r, g, b,newAlpha);
	}
}
