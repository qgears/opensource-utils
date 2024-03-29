package hu.qgears.parser.editor.coloring;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
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
	public StyleRange toStyleRange(Range r, int length) {
		int to=r.to;
		if(to>length)
		{
			to=length;
		}
		if(to>r.from)
		{
			SwtStyle st=findStyle(r.styleId);
			if(st!=null)
			{
				StyleRange range=new StyleRange(r.from, to-r.from, st.fg, st.bg, st.style);
				return range;
			}
		}
		return null;
	}
	/**
	 * Convert Qparser style representation to SWT style representation
	 * @param coloring
	 * @param length current length of the document - style ranges exceeding this are cut. Cropping is necessary because asynchronity betweeen editor and model is possible
	 * and out of range exception destroys the editor for some reason instead of handling gracefully.
	 * @param iDocument 
	 * @return
	 */
	public TextPresentation getTextParameters(StyleBasedColoring coloring, int length, IDocument iDocument) {
		TextPresentation tp=new TextPresentation();
		ColoringLogger log=new ColoringLogger();
		for(int from: coloring.getFroms().keySet())
		{
			Range r=coloring.getFroms().get(from);
			StyleRange se=toStyleRange(r, length);
			if(se!=null)
			{
				tp.addStyleRange(se);
				if(log!=null)
				{
					log.addStyleRange(se, iDocument);
				}
			}
		}
		if(log!=null)
		{
			log.print();
		}
		return tp;
	}
	private SwtStyle findStyle(String styleId) {
		return styles.get(styleId);
	}
}
