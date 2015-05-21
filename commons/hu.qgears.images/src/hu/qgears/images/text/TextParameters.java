package hu.qgears.images.text;

import java.lang.reflect.Field;

/**
 * All paramters of a text label to be rendered. Using these parameters a label
 * can be rendered onto a bitmap
 * 
 * @author rizsi
 *
 */
public class TextParameters {

	private static final String SYSPROP_NAME_WBF_DEFAULT_FONT = "wbf.defaultFont";
	/**
	 * The default system wide fontface
	 * 
	 * @since 3.0
	 */
	public static final String DEFAULT_FONT_FACE = System.getProperty(
			SYSPROP_NAME_WBF_DEFAULT_FONT, "Liberation Sans");

	/**
	 * Default zero letter spacing
	 * 
	 * @since 3.0
	 */
	public static final double DEFAULT_LETTER_SPACING = 0.0d;
	/**
	 * The default fontsize in pixel.
	 * 
	 * @since 3.0
	 */
	public static final float DEFAULT_FONT_SIZE = 19.0f;
	/**
	 * The default text {@link WColor color}
	 * 
	 * @since 3.0
	 */
	public static final RGBAColor DEFAULT_COLOR = RGBAColor.WHITE;
	/**
	 * The default {@link VerticalAlignment} of the text
	 * 
	 * @since 3.0
	 */
	public static final EVerticalAlign DEFAULT_VERTICAL_ALIGNMENT = EVerticalAlign.top;
	/**
	 * The default {@link HorizontalAlignment} of the text.
	 * 
	 * @since 3.0
	 */
	public static final EHorizontalAlign DEFAULT_HORIZONTAL_ALIGNMENT = EHorizontalAlign.left;

	/**
	 * The name of the font family
	 */
	public String fontFamily = DEFAULT_FONT_FACE;
	/**
	 * The size of the font in HTML pixels
	 */
	public float fontSize = DEFAULT_FONT_SIZE;
	/**
	 * The foreground color of the text
	 */
	public RGBAColor c = DEFAULT_COLOR;

//	/**
//	 * The height of the text container.
//	 * 
//	 * TODO is dimension relevant in this DTO???
//	 */
//	public int height = 0;
//	public int width = 0;
	/**
	 * The character data content of the label
	 */
	public String text = "";
	/**
	 * The weight of the font. See {@link EFontWeight}
	 */
	public EFontWeight fontWeight = EFontWeight.normal;
	/**
	 * The font style. See {@link EFontStyle}
	 */
	public EFontStyle fontStyle = EFontStyle.normal;
	/**
	 * The horizontal alignment of the label within its container.
	 */
	public EHorizontalAlign hAlign = EHorizontalAlign.left;
	/**
	 * The vertical alignment of the label within its container.
	 */
	public EVerticalAlign vAlign = EVerticalAlign.top;
	/**
	 * The decoration on label. See {@link ETextDecoration}
	 */
	public ETextDecoration textDecoration = ETextDecoration.none;
//	/**
//	 * TODO : this field should be removed -> a new FlowTextParameters DTO
//	 * should be created for rendering flowTexts.
//	 */
//	public boolean richText = false;
	/**
	 * The distance between adjacent characters in HTML pixels.
	 */
	public double letterSpacing = 0;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextParameters) {
			TextParameters other = (TextParameters) obj;
			return eq(other.fontFamily, fontFamily)
					&& other.fontSize == fontSize && other.c.equals(c)
//					&& other.height == height && other.width == width
					&& eq(other.text, text) && other.fontWeight == fontWeight
					&& other.fontStyle == fontStyle && other.hAlign == hAlign
					&& other.vAlign == vAlign
					&& other.textDecoration == textDecoration
//					&& other.richText == richText
					&& other.letterSpacing == letterSpacing;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash(fontFamily) ^ hash(fontSize) ^ hash(c)/* ^ height ^ width*/
				^ hash(text) ^ hash(fontWeight) ^ hash(fontStyle)
				^ hash(vAlign) ^ hash(hAlign) ^ hash(textDecoration)
				^ hash(letterSpacing);
	}
	
	/**
	 * @since 3.0
	 */
	protected static int hash(Object o){
		return o == null ? 0 : o.hashCode();
	}
	
	public TextParameters() {
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param toCopy
	 * @since 3.0
	 */
	public TextParameters(TextParameters toCopy) {
		updateFrom(toCopy);
	}
	
	/**
	 * Copy values from specified textParamters.
	 * 
	 * @since 3.0
	 */
	public void updateFrom(TextParameters toCopy) {
		this.fontFamily = toCopy.fontFamily;
		this.fontSize = toCopy.fontSize;
		this.c  = new RGBAColor(toCopy.c.r,toCopy.c.g,toCopy.c.b,toCopy.c.a);
//		this.height = toCopy.height;
//		this.width = toCopy.width;
		this.text = toCopy.text;
		this.fontWeight = toCopy.fontWeight;
		this.fontStyle= toCopy.fontStyle;
		this.hAlign = toCopy.hAlign;
		this.vAlign = toCopy.vAlign;
		this.textDecoration = toCopy.textDecoration;
//		this.richText= toCopy.richText;
		this.letterSpacing = toCopy.letterSpacing;
	}
	
	
	private static final boolean eq(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		Field[] fd = getClass().getFields();
		for (Field f : fd) {
			try {
				Object v = f.get(this);
				String value;
				if (v != null) {
					value = v.toString();
				} else {
					value = "null";
				}
				bld.append(f.getName()).append("=").append(value).append(";");
			} catch (Exception e) {
				// should not happen
				e.printStackTrace();
			}
		}
		return bld.toString();
	}

}
