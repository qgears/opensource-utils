package hu.qgears.parser.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import hu.qgears.parser.contentassist.CompletitionProposalResult;
import hu.qgears.parser.contentassist.ICompletitionProposalContext;
import hu.qgears.parser.contentassist.ProjectContentAssistProcessor;
import hu.qgears.parser.contentassist.QCompletionProposal;

public class QContentAssistProcessor implements IContentAssistProcessor {
	ProjectContentAssistProcessor pcap=new ProjectContentAssistProcessor();
	ICompletitionProposalContext proposalContext;
	public QContentAssistProcessor(AbstractQParserEditor editor) {
		proposalContext = editor.getProposalContext();
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		if(proposalContext!=null)
		{
			String text=viewer.getDocument().get();
			CompletitionProposalResult res=pcap.computeCompletionProposals(proposalContext, text, offset);
			List<ICompletionProposal> ret=new ArrayList<ICompletionProposal>();
			for(QCompletionProposal p: res.getProposals())
			{
				ret.add(new CompletionProposal(p.toInsert, offset-p.overWriteNchars, p.overWriteNchars, p.toInsert.length()));
			}
			// hu.qgears.parser.contentassist.CompletionProposal
			return ret.toArray(new ICompletionProposal[]{}); 
		}else
		{
			return new ICompletionProposal[]{
				};
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return new IContextInformation[]{};
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[]{};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[]{};
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return new IContextInformationValidator() {
			
			@Override
			public boolean isContextInformationValid(int offset) {
				return false;
			}
			
			@Override
			public void install(IContextInformation info, ITextViewer viewer, int offset) {
				
			}
		};
	}

}
