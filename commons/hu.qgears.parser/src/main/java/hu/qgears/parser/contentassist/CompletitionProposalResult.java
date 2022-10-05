package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.List;

public class CompletitionProposalResult {
	private List<CompletionProposal> proposals=new ArrayList<>();
	public void add(CompletionProposal completionProposal) {
		proposals.add(completionProposal);
	}
	public List<String> getList() {
		List<String> ret=new ArrayList<>();
		for(CompletionProposal p: proposals)
		{
			ret.add(p.toInsert);
		}
		return ret;
	}
}
