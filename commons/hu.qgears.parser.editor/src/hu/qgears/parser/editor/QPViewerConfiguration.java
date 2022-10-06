package hu.qgears.parser.editor;

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
	public QPViewerConfiguration(AbstractQParserEditor editor) {
		hyperlinkDetectors=new IHyperlinkDetector[]{new QPHyperlinkDetector(editor)};
	}
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return hyperlinkDetectors;
	}
}
