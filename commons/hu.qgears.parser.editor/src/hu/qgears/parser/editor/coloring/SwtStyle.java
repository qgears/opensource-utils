package hu.qgears.parser.editor.coloring;

import org.eclipse.swt.graphics.Color;

/**
 * DTO class to store possible styling of text.
 */
public class SwtStyle {
	public final int style;
	public final Color fg;
	public final Color bg;
	public final String id;
	public SwtStyle(String id, Color fg, Color bg, int style) {
		this.id=id;
		this.fg = fg;
		this.bg = bg;
		this.style = style;
	}
}
