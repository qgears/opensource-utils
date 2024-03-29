package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermAnd;
import hu.qgears.parser.language.impl.TermOneOrMore;
import hu.qgears.parser.language.impl.TermOr;
import hu.qgears.parser.language.impl.TermRef;
import hu.qgears.parser.language.impl.TermToken;
import hu.qgears.parser.language.impl.TermZeroOrMore;

public class ParserIntegration {
	public static PossibleCollector collectPossibleOngoing(ElemBuffer buffer, int groupOffsetToLast, ICompletitionProposalContext proposalContext) {
		PossibleCollector pc=new PossibleCollector();
		int lastGroup=buffer.getCurrentGroup();
		if(lastGroup>=0)
		{
			int start=buffer.getGroupStart(lastGroup);
			int end=buffer.getGroupEnd(lastGroup);
			// System.out.println(buffer.print());
			for(int i=start;i<end;++i)
			{
				int dotPos=buffer.getDotPosition(i);
				Term te=buffer.resolve(buffer.getTermTypeId(i));
				pc.collectPossible(te, dotPos);
				proposalContext.notifyParseState(buffer, i);
			}
		}
		return pc;
	}
	private ElemBuffer buffer;
	int groupOffsetFromEnd;
	String prefix;
	List<String> context;
	ICompletitionProposalContext proposalContext;
	public ParserIntegration(ICompletitionProposalContext proposalContext, ElemBuffer buffer, int groupOffsetFromEnd, String prefix, List<String> context) {
		this.proposalContext=proposalContext;
		this.buffer=buffer;
		this.groupOffsetFromEnd=groupOffsetFromEnd;
		this.prefix=prefix;
		this.context=new ArrayList<>(context);
	}
	/**
	 * Prevent recursion.
	 */
	private Set<Term> met=new HashSet<>();
	/**
	 * Store parent types.
	 */
	private List<Term> parents=new ArrayList<>();
	public void getAllowedPrefixes(PossibleGoon collect, Term t) {
		if(proposalContext.collectAllowedPrefixes(collect, t, buffer, prefix, context, parents))
		{
			return;
		}
//		if("reference".equals(t.getName()))
//		{
//			CircuitModelInMemory cmm=IncrementalBuilder.currentParsed.getProperty();
//			if(cmm!=null)
//			{
//				List<TreeElem> tes=BuildTree.findElements(t, buffer.getCurrentGroup(), buffer);
//				CrossRefManager crm=cmm.csb.crm;
//				Set<String> pnames=crm.getObjectNames();
//				for(String p: pnames)
//				{
//					for(int i=context.size();i>=0;--i)
//					{
//						String pref0=UtilString.concat(context.subList(0, i), ".");
//						String pref;
//						if(prefix.length()>0 && pref0.length()>0)
//						{
//							pref=pref0+"."+prefix;
//						}else
//						{
//							pref=pref0+prefix;
//						}
//						if(p.startsWith(pref))
//						{
//							String h=p.substring(pref0.length());
//							if(h.startsWith("."))
//							{
//								h=h.substring(1);
//							}
//							collect.add(h, "Package");
//						}
//					}
//				}
//				for(TreeElem te: tes)
//				{
//					collect.add("REFERENCE: "+te);
//				}
//			}
//			collect.add("REFERENCE");
//			return;
//		}
		if(met.add(t))
		{
			parents.add(t);
			try
			{
				switch (t.getType()) {
				case token:
					TermToken tt=(TermToken)t;
					//t.getLanguage().getTokenizerDef().
					Matcher m=tt.getMatchingValue();
					if(m!=null)
					{
						List<String> p=new ArrayList<>();
						m.collectPossibleValues(p);
						for(String s: p)
						{
							collect.addContentAssistProposal(s);
						}
					}else
					{
						// ITokenType tokenType=tt.getTokenType();
						proposalContext.collectPossibilities(collect, tt, "");
					}
					break;
				case and:
					for(Term x: ((TermAnd)t).getSubs())
					{
						getAllowedPrefixes(collect, x);
						// TODO handle case when inside there is an epsilon...
						break;
					}
					break;
				case or:
					for(Term x: ((TermOr)t).getSubs())
					{
						getAllowedPrefixes(collect, x);
					}
					break;
				case epsilon:
					break;
				case oneormore:
					getAllowedPrefixes(collect, ((TermOneOrMore)t).getSub());
					break;
				case reference:
					getAllowedPrefixes(collect, ((TermRef)t).getSub());
					break;
				case zeroormore:
					getAllowedPrefixes(collect, ((TermZeroOrMore)t).getSub());
					break;
				default:
					break;
				}
			}finally
			{
				parents.remove(t);
			}
		}
	}
}
