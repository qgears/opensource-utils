package hu.qgears.images.text;

import org.apache.log4j.Logger;

/**
 * @since 3.0
 */
public class TextUtils {

	private static final Logger LOG = Logger.getLogger(TextUtils.class);
	
	private TextUtils() {
		// disable constructor of utility class
	}
	/**
	 * Returns the necessary disposition of 'y' coordinate, to get the specified alignment.
	 * 
	 * @param vAlign The {@link EVerticalAlign} to render
	 * @param height The height of container into the label must fit
	 * @param textHeight The height of the rendered label
	 * @return
	 */
	public static int alignVertically(EVerticalAlign vAlign, double height, double textHeight) {
		int dispositionY=0;
		switch(vAlign)
		{
		case top:	// top
			dispositionY=0;
			break;
		case bottom: // bottom
			dispositionY=(int) (height-textHeight);
			if(dispositionY<0)
			{
				dispositionY=0;
			}
			break;
		default: // middle
			/*
			 * Reproducing behavior of the web browser: if the text is 
			 * too tall to fit into the bounding box, its top will not
			 * be cropped, instead it will be pushed downwards. (So, in 
			 * special cases, only the bottom will be cropped.)  
			 */
			dispositionY = Math.max((int) ((height-textHeight)/2.0d), 0);
			break;
		}
		return dispositionY;
	}

	/**
	 * Returns the necessary disposition of 'x' coordinate, to get the specified alignment.
	 * 
	 * @param hAlign The {@link EHorizontalAlign} to render
	 * @param width The width of container into the label must fit
	 * @param textWidth The width of the rendered label
	 * @return
	 */
	public static int alignHorizontally(EHorizontalAlign hAlign, double width, double textWidth) {
		int dispositionX=0;
		switch(hAlign)
		{
		case left:
			dispositionX=0;
			break;
		case right:
			dispositionX=(int) (width-textWidth);
			if(dispositionX<0)
			{
				dispositionX=0;
			}
			break;
		default:
			/*
			 * Reproducing behavior of the web browser: if the text is 
			 * too tall to fit into the bounding box, its top will not
			 * be cropped, instead it will be pushed downwards. (So, in 
			 * special cases, only the bottom will be cropped.)  
			 */
			dispositionX = Math.max((int) ((width-textWidth)/2.0d), 0);
			break;
		}
		return dispositionX;
	}

}
