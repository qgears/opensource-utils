package hu.qgears.parser.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class QPHyperlinkDetector implements IHyperlinkDetector {
	private AbstractQParserEditor editor;
	public QPHyperlinkDetector(AbstractQParserEditor editor) {
		this.editor=editor;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		// System.out.println("Detect hyperlinks: "+textViewer+" "+region+" "+canShowMultipleHyperlinks);
		return editor.findLink(region);
	}
}
