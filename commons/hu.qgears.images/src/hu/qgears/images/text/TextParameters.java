package hu.qgears.images.text;

import java.lang.reflect.Field;

/**
 * All paramters of a text label to be rendered.
 * Using these parameters a label can be rendered onto a bitmap
 * @author rizsi
 *
 */
public class TextParameters {
	public static final String defaultFontFamily="Liberation Sans";
	public String fontFamily=defaultFontFamily;
	public float fontSize=19;
	public TextColor c=new TextColor(255, 255, 255, 255);
	public int height=0;
	public int width=0;
	public String text="";
	
	public EFontWeight fontWeight=EFontWeight.normal;
	public EFontStyle fontStyle=EFontStyle.normal;
	public EHorizontalAlign hAlign=EHorizontalAlign.left;
	public EVerticalAlign vAlign=EVerticalAlign.top;
	public ETextDecoration textDecoration=ETextDecoration.none;
	public boolean richText = false;
	public int letterSpacing = 0;
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TextParameters)
		{
			TextParameters other=(TextParameters) obj;
			return eq(other.fontFamily,fontFamily)&&
					other.fontSize==fontSize&&
					other.c.equals(c)&&other.height==height
					&&other.width==width&&
					eq(other.text, text)&&
					other.fontWeight==fontWeight&&
					other.fontStyle==fontStyle&&
					other.hAlign==hAlign&&
					other.vAlign==vAlign&&
					other.textDecoration==textDecoration&&
					other.richText==richText&&
					other.letterSpacing==letterSpacing;
		}
		return false;
	}
	
	private static final boolean eq(String a, String b) {
		return a==null?b==null:a.equals(b);
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		Field[] fd=getClass().getFields();
		for(Field f: fd){
			try {
				Object v = f.get(this);
				String value;
				if (v != null){
					value = v.toString();
				} else {
					value = "null";
				}
				bld.append(f.getName()).append("=").append(value).append(";");
			} catch (Exception e) {
				//should not happen
				e.printStackTrace();
			}
		}
		return bld.toString();
	}
	
	
}
