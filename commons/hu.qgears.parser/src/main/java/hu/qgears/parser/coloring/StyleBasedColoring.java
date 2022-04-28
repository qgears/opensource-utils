package hu.qgears.parser.coloring;

import java.util.SortedMap;
import java.util.TreeMap;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.util.TreeVisitor;

public class StyleBasedColoring {
	protected TreeMap<Integer, Range> froms=new TreeMap<>();
	private TreeMap<Integer, Range> tos=new TreeMap<>();
	private int length;
	private class ParserCallback implements IParserReceiver {
		public void tokensUnfiltered(java.util.List<IToken> tokensUnfiltered) {
			for(IToken t: tokensUnfiltered)
			{
				String name=t.getTokenType().getName();
				String styleId=styleBasedColoringConfiguration.tokenToStyle.get(name);
				if(styleId!=null)
				{
					addRange(t.getPos(), t.getPos()+t.getLength(), styleId);
				}
			}
		};
		public void treeUnfiltered(hu.qgears.parser.impl.TreeElem root) {
			try {
				new TreeVisitor() {
					@Override
					protected AutoCloseable visitNode(ITreeElem te, int depth) throws Exception {
						String styleId=styleBasedColoringConfiguration.typeToStyle.get(te.getTypeName());
						if(styleId!=null)
						{
							addRange(te.getTextIndexFrom(), te.getTextIndexTo(), "keyword");
						}
						return ()->{};
					}
				}.visit(root);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public IParserReceiver createParserCallback()
	{
		return new ParserCallback();
	}
	private StyleBasedColoringConfiguration styleBasedColoringConfiguration;
	public StyleBasedColoring(StyleBasedColoringConfiguration styleBasedColoringConfiguration)
	{
		this.styleBasedColoringConfiguration=styleBasedColoringConfiguration;
	}
	public void reset(int length)
	{
		this.length=length;
		Range all=new Range(0, length, null);
		froms.clear();
		tos.clear();
		put(all);
	}
	private void put(Range r) {
		froms.put(r.from, r);
		tos.put(r.to, r);
	}
	public void addRange(int from, int to, String styleId)
	{
		if(to>from && from>=0 && to<=length)
		{
				Range a=froms.floorEntry(from).getValue();
				Range b=tos.ceilingEntry(to).getValue();
				removeRange(a);
				removeRange(b);
				SortedMap<Integer, Range> toDelete=froms.subMap(from, to);
				while(!toDelete.isEmpty())
				{
					removeRange(toDelete.get(toDelete.firstKey()));
				}
				if(from>a.from)
				{
					Range aSplit=new Range(a.from, from, a.styleId);
					put(aSplit);
				}
				if(b.to>to)
				{
					Range bSplit=new Range(to, b.to, b.styleId);
					put(bSplit);
				}
				put(new Range(from, to, styleId));
		}
	}
	private void removeRange(Range a) {
		froms.remove(a.from);
		tos.remove(a.to);
	}
	public TreeMap<Integer, Range> getFroms() {
		return froms;
	}
}