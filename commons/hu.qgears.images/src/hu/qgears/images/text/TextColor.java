package hu.qgears.images.text;


public class TextColor {
	private static final float TO_FLOAT_MULTIPLIER =  1.0f / 255.0f;
	final public int r, g, b, a;

	public TextColor(int r, int g, int b, int a) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	public static final TextColor fromFloats(float r_f,float g_f,float b_f,float a_f) {
		assertValidFloat(r_f);
		assertValidFloat(g_f);
		assertValidFloat(b_f);
		assertValidFloat(a_f);
		int r = Math.round(r_f / TO_FLOAT_MULTIPLIER);
		int g = Math.round(g_f / TO_FLOAT_MULTIPLIER);
		int b = Math.round(b_f / TO_FLOAT_MULTIPLIER);
		int a = Math.round(a_f / TO_FLOAT_MULTIPLIER);
		return new TextColor(r, g, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TextColor)
		{
			TextColor other=(TextColor) obj;
			return other.r==r&&other.g==g&&other.b==b&&other.a==a;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return r^(g<<8)^(b<<16)^(a<<24);
	}
	/**
	 * Converts this color to 'rgba(r,g,b,a)' format.
	 * @return
	 */
	public String toCssParameter(){
		return "rgba("+r+","+g+","+b+","+a+")";
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
	
	/**
	 * Updates this DTO based on specified float color coordinates.
	 * 
	 * @param r_f Value of red channel (0f .. 1f)
	 * @param g_f Value of green channel (0f .. 1f)
	 * @param b_f Value of blue channel (0f .. 1f)
	 * @param a_f Value of alpha channel (0f .. 1f)
	 * 
	 * @throws IllegalArgumentException if one of the the specified values are out of range.
	 * @see #toFloatVector()
	 */
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
	
	@Override
	public String toString() {
		return toCssParameter();
	}
}
