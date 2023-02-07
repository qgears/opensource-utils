package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.List;

public class CompletitionProposalResult {
	private List<QCompletionProposal> proposals=new ArrayList<>();
	public void add(QCompletionProposal completionProposal) {
		proposals.add(completionProposal);
	}
	public List<String> getList() {
		List<String> ret=new ArrayList<>();
		for(QCompletionProposal p: proposals)
		{
			ret.add(p.toInsert);
		}
		return ret;
	}
	public List<QCompletionProposal> getProposals() {
		return proposals;
	}
}
