package hu.qgears.parser.editor.coloring;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

import hu.qgears.parser.coloring.Range;
import hu.qgears.parser.coloring.StyleBasedColoring;
import hu.qgears.parser.coloring.StyleBasedColoringConfiguration;

/**
 * Default coloring implementation of text editors.
 * Maps style identifiers to {@link SwtStyle} objects.
 * See {@link StyleBasedColoring}
 * {@link StyleBasedColoringConfiguration}
 */
public class SwtStyleBasedColoring {
	private Map<String, SwtStyle> styles=new HashMap<>();

	public SwtStyleBasedColoring() {
		styles.put(null, new SwtStyle("basic", null, null, 0));
	}
	public void registerStyle(SwtStyle swtStyle) {
		styles.put(swtStyle.id, swtStyle);
	}
	public StyleRange toStyleRange(Range r) {
		SwtStyle st=findStyle(r.styleId);
		if(st!=null)
		{
			StyleRange range=new StyleRange(r.from, r.to-r.from, st.fg, st.bg, st.style);
			return range;
		}
		return null;
	}
	public TextPresentation getTextParameters(StyleBasedColoring coloring) {
		TextPresentation tp=new TextPresentation();
		for(int from: coloring.getFroms().keySet())
		{
			Range r=coloring.getFroms().get(from);
			StyleRange se=toStyleRange(r);
			if(se!=null)
			{
				tp.addStyleRange(se);
			}
		}
		return tp;
	}
	private SwtStyle findStyle(String styleId) {
		return styles.get(styleId);
	}
}
