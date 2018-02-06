package hu.qgears.images.text;


/**
 * POJO that describes the the content and the horizontal alignment of a
 * paragraph in flowtexts. The content can be either plain text, or pango
 * markup, which can be directly rendered to richtext by using cairo.
 * 
 * @author agostoni
 * 
 */
public class ParagraphInfo {
	
	/**
	 * Default margin in 'p' HTML elements, which is used in Chrome's default CSS style sheet.
	 * The value's measurement unit is 'em', which is equals the current font size in 'px'.
	 * 
	 * <br> The full style sheet is available on the WebKit trac.
	 * http://trac.webkit.org/browser/trunk/Source/WebCore/css/html.css
	 */
	public static final double marginBeforeDefault=1.0;
	
	/**
	 * See {@link #marginBefore}
	 */
	public static final double marginAfterDefault=0.0;
	

	private String pangoMarkupContent;

	private EHorizontalAlign horizontalAlign;

	private int height;

	private double marginBefore = marginBeforeDefault;
	
	private double marginAfter = marginAfterDefault;
	
	private int width;

	private EVerticalAlign verticalAligment;

	public ParagraphInfo() {
		this(EVerticalAlign.bottom);
	}
	public ParagraphInfo(EVerticalAlign va) {
		this.verticalAligment = va;
	}

	public ParagraphInfo(String pangoMurkupContent,
			EHorizontalAlign horizontalAlign) {
		super();
		this.pangoMarkupContent = pangoMurkupContent;
		this.horizontalAlign = horizontalAlign;
	}

	public String getPangoMurkupContent() {
		return pangoMarkupContent;
	}

	public void setPangoMarkupContent(String pangoMurkupContent) {
		this.pangoMarkupContent = pangoMurkupContent;
	}

	public EHorizontalAlign getHorizontalAlign() {
		return horizontalAlign;
	}

	public void setHorizontalAlign(EHorizontalAlign horizontalAlign) {
		this.horizontalAlign = horizontalAlign;
	}

	public void setHeight(int textHeightInPixel) {
		this.height = textHeightInPixel;
	}

	/**
	 * Returns the height of the text in the paragraph (without margins). The
	 * height will be 0, if the paragraph is not rendered yet!
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	public void setWidth(int textWidthInPixel) {
		this.width = textWidthInPixel;
	}

	/**
	 * Returns the width of the paragraph. The paragraph must be rendered first!
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	public double getMarginAfter() {
		return marginAfter;
	}
	
	public double getMarginBefore() {
		return marginBefore;
	}

	public void setMarginBefore(double marginBefore) {
		this.marginBefore = marginBefore;
	}

	public void setMarginAfter(double marginAfter) {
		this.marginAfter = marginAfter;
	}
	
	public EVerticalAlign getVerticalAligment() {
		return verticalAligment;
	}
	
	public void setVerticalAligment(EVerticalAlign verticalAligment) {
		this.verticalAligment = verticalAligment;
	}
}
