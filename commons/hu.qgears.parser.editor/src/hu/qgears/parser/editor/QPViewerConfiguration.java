package hu.qgears.parser.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class QPViewerConfiguration extends SourceViewerConfiguration {
//	@Override
//	public IPresentationReconciler getPresentationReconciler(ISourceViewer viewer) {
//		PresentationReconciler reconciler= new PresentationReconciler();
//		DefaultDamagerRepairer dflt= new DefaultDamagerRepairer(createTokenScanner());
//		reconciler.setDamager(dflt, IDocument.DEFAULT_CONTENT_TYPE);
//		reconciler.setRepairer(dflt, IDocument.DEFAULT_CONTENT_TYPE);
//		return reconciler;
//	}
//	private ITokenScanner createTokenScanner() {
//		RuleBasedScanner scanner= new RuleBasedScanner();
//		scanner.setRules(createRules());
//		return scanner;
//	}
//	private IRule[] createRules() {
//		IToken tokenA= new Token(new TextAttribute(getBlueColor());
//		IToken tokenB= new Token(new TextAttribute(getGrayColor());
//		return new IRule[] {
//		new PatternRule(">", "<", tokenA, '\\', false),
//		new EndOfLineRule("-- ", tokenB)
//		};
//	}
	private IHyperlinkDetector[] hyperlinkDetectors;
	private ContentAssistant contentAssistant=new ContentAssistant();
	public QPViewerConfiguration(AbstractQParserEditor editor) {
		hyperlinkDetectors=new IHyperlinkDetector[]{new QPHyperlinkDetector(editor)};
		QContentAssistProcessor proc=new QContentAssistProcessor(editor);
		// contentAssistant.
		contentAssistant.setContentAssistProcessor (proc, 
				IDocument.DEFAULT_CONTENT_TYPE);
/*		contentAssistant.setContentAssistProcessor (proc, 
				"com.bbraun.spaceii.qparser.editor.dslgui");*/
//		contentAssistant.set
	}
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return hyperlinkDetectors;
	}
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return contentAssistant;
	}
}
