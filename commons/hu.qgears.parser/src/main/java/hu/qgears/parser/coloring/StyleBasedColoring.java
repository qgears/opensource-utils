package hu.qgears.parser.coloring;

import java.util.SortedMap;
import java.util.TreeMap;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.ParseException;
import hu.qgears.parser.tokenizer.Token;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.util.TreeVisitor;

/**
 * See also SwtStyleBasedColoring
 */
public class StyleBasedColoring {
	protected TreeMap<Integer, Range> froms=new TreeMap<>();
	private TreeMap<Integer, Range> tos=new TreeMap<>();
	private int length;
	public class ParserCallback implements IParserReceiver {
		public ParseErrorFeedback parseErrorFeedback;
		@Override
		public void stucked(ElemBuffer buffer, TokenArray tokens, int tIndex) throws ParseException {
			parseErrorFeedback=new ParseErrorFeedback("Can not parse", tokens.pos(tIndex), tokens.length(tIndex));
		}
		@Override
		public void parseProblemUnknown(ElemBuffer buffer) throws ParseException {
			parseErrorFeedback=new ParseErrorFeedback("Can not parse", 0, 1);
		}
		@Override
		public void tokenizeError(TokenizerException exc) throws TokenizerException {
			parseErrorFeedback=new ParseErrorFeedback(exc.getMessage(), exc.getPosition(), 1);
		}
		public void tokensUnfiltered(java.util.List<Token> tokensUnfiltered) {
			for(Token t: tokensUnfiltered)
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
	public ParserCallback createParserCallback()
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