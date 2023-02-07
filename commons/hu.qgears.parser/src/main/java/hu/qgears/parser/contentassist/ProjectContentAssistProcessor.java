package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.util.TreeVisitor;

public class ProjectContentAssistProcessor {
	private List<IToken> lasts=new ArrayList<>();
	private String textPreCursor;
	private String textPreSemanticPoint;
	private String textIdentifierRemovedAndAdded;
	private static final String markerException="Normal Exit";
	
	private static final String keyCThis="cThis";
	private static final String keyPackageDef="packageDef";//  CircuitSemanticBuilder.packageDef
	public CompletitionProposalResult computeCompletionProposals(ICompletitionProposalContext proposalContext, String text, int offset) {
		textPreCursor=text.substring(0,offset);
		lasts.clear();
		CompletitionProposalResult ret=new CompletitionProposalResult();
		try {
			proposalContext.parse(textPreCursor, new IParserReceiver() {
				@Override
				public void tokensUnfiltered(List<IToken> tokensUnfiltered) {
					if(tokensUnfiltered.size()>0)
					{
						if(tokensUnfiltered.size()>0 && endsWithId(tokensUnfiltered, "cThis"))
						{
							lasts.add(tokensUnfiltered.remove(tokensUnfiltered.size()-1));
						}
						while(tokensUnfiltered.size()>0 && endsWithId(tokensUnfiltered, "tId"))
						{
							IToken last=removeLastIdAndPossibleWhiteSpaces(tokensUnfiltered);
						}
						System.out.println("Removed tokens for parse: '"+lasts+"'");
					}
					throw new RuntimeException(markerException);	// Restart parsing after finding out last tokens
				}
				private IToken removeLastIdAndPossibleWhiteSpaces(List<IToken> tokensUnfiltered) {
					IToken ret=tokensUnfiltered.remove(tokensUnfiltered.size()-1);
					IToken dot=null; 
					int i;
					for(i=tokensUnfiltered.size()-1; i>=0; --i)
					{
						IToken t=tokensUnfiltered.get(i);
						if(proposalContext.isFiltered(t))
						{
							// Go on
						}else if(keyCThis.equals(t.getTokenType().getName()))
						{
							if(dot!=null)
							{
								return ret;
							}
							dot=t;
						}else
						{
							i++;
							break;
						}
					}
					lasts.add(0,ret);
					if(dot!=null)
					{
						lasts.add(0, dot);
						for(;i<tokensUnfiltered.size();++i)
						{
							tokensUnfiltered.remove(tokensUnfiltered.size()-1);
						}
					}
					return ret;
				}
				private boolean endsWithId(List<IToken> tokensUnfiltered, String type) {
					IToken last=tokensUnfiltered.get(tokensUnfiltered.size()-1);
					return type.equals(last.getTokenType().getName());
				}
			});
		} catch (Exception e) {
			if(e instanceof RuntimeException && markerException.equals(e.getMessage()))
			{
				// Normal exit :-)
			}else
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        if(lasts.size()>0)
        {
            textPreSemanticPoint=text.substring(0, lasts.get(0).getPos());
            System.out.println(textPreSemanticPoint);
        }else
        {
        	textPreSemanticPoint=textPreCursor;
        }
        List<String> context=new ArrayList<>();
        textIdentifierRemovedAndAdded=textPreSemanticPoint+" dummyPlaceHolder "+text.substring(textPreCursor.length());
        try {
			proposalContext.parse(textIdentifierRemovedAndAdded, new IParserReceiver() {
				List<String> contextPackage=new ArrayList<>();
				@Override
				public void treeFiltered(TreeElem root) {
					try {
						new TreeVisitor() {
							@Override
							protected AutoCloseable visitNode(ITreeElem tree, int depth) throws Exception {
								if(tree.getTextIndexFrom()>=textPreSemanticPoint.length())
								{
									context.addAll(contextPackage);
									throw new RuntimeException(markerException);
								}
								AutoCloseable ret=proposalContext.collectCurrentStateOfText(tree, depth);
								return ret;
//								if(keyPackageDef.equals(tree.getTypeName()))
//								{
//									List<String> packs=CircuitSemanticBuilder.getFqId(tree.getSubs().get(0));
//									contextPackage.addAll(packs);
//									return ()->{
//										removeFromList(contextPackage, packs.size());};
//								}
//								if(CircuitSemanticBuilder.isDefinitionType(tree))
//								{
//									String id=CircuitSemanticBuilder.getId(tree);
//									contextPackage.add(id);
//									return ()->{
//										removeFromList(contextPackage, 1);};
//								}
//								return ()->{};
							}

							private void removeFromList(List<String> l, int size) {
								for(int i=0;i<size;++i)
								{
									l.remove(l.size()-1);
								}
							}
						}.visit(root);
					} catch (Exception e) {
						if(e instanceof RuntimeException && markerException.equals(e.getMessage()))
						{
							// Normal exit :-)
						}else
						{
							proposalContext.logError(e);
						}
					}
					System.out.println("Identifier context parse success! "+context);
				}
			});
		} catch (Exception e) {
			if(e instanceof RuntimeException && markerException.equals(e.getMessage()))
			{
				// Normal exit :-)
			}else
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        try {
			proposalContext.parse(textPreSemanticPoint, new IParserReceiver() {
				@Override
				public void tableFilled(ElemBuffer buffer, int size) {
					// System.out.println("Table filled!");
					String prefix="";
					for(IToken t: lasts)
					{
						prefix+=t.getText().toString();
					}
					// System.out.println("prefix: "+prefix);
					PossibleCollector pc=ParserIntegration.collectPossibleOngoing(buffer,0);
					Map<String, PossibleGoon> byToken=new TreeMap<>();
					List<PossibleGoon> pgs=new ArrayList<>();
					for(Term t: pc.found)
					{
						String name=t.getName();
						PossibleGoon pg=new PossibleGoon(t);
						pgs.add(pg);
						ParserIntegration pi=new ParserIntegration(proposalContext, buffer, 0, prefix, context);
						pi.getAllowedPrefixes(pg, t);
						for(String p: pg.prefixes)
						{
							if(p.startsWith(prefix))
							{
								byToken.put(p, pg);
							}
						}
					}
					for(String key: byToken.keySet())
					{
						String toInsert=key.substring(prefix.length());
						PossibleGoon pg=byToken.get(key);
						ret.add(new QCompletionProposal(key, offset-prefix.length(), prefix.length(), toInsert.length(), null, key, null, ""+pg.t.getName()));
					}
					throw new RuntimeException(markerException);
				}
			});
		} catch (Exception e) {
			if(e instanceof RuntimeException && markerException.equals(e.getMessage()))
			{
				// Normal exit :-)
			}else
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        return ret;
    }
}