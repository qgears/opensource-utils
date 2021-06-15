package hu.qgears.parser.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermAnd;
import hu.qgears.parser.language.impl.TermMore;
import hu.qgears.parser.language.impl.TermOr;
import hu.qgears.parser.language.impl.TermRef;
import hu.qgears.parser.tokenizer.IToken;



/**
 * After creating the table of early parse built the AST tree by reading it up.
 * 
 * @author rizsi
 * 
 */
public class BuildTree {
	private ParserLogger logger;
	public BuildTree(ParserLogger logger) {
		super();
		this.logger = logger;
	}


	void buildTreeRoot(TreeElem root) throws ParseException {
		buildTree(root);
	}

	void buildTree(TreeElem root) throws ParseException {
		List<TreeElem> chs = buildChildrenList(root);
		root.setSubs(chs);
		for (TreeElem te : chs) {
			buildTree(te);
		}
	}

	/**
	 * Build the children list of a tree element.
	 * @param e parent element
	 * @return the children of the given element.
	 * @throws ParseException In case the children can not be found - can not find or ambigous
	 */
	List<TreeElem> buildChildrenList(TreeElem e) throws ParseException {
		/**
		 * The types of elements that build up this element that is to be 
		 * resolved to children.
		 */
		List<Term> types = new ArrayList<Term>(e.dotPos);
		Term termType=e.getType();
		switch (termType.getType()) {
		case and: {
			TermAnd and = (TermAnd) termType;
			types.addAll(and.getSubs());
			break;
		}
		case or: {
			TermOr or = (TermOr) termType;
			types.add(or.getSubs().get(e.choice));
			break;
		}
		case zeroormore:
		case oneormore: {
			if (e.dotPos == 0) {
				return new ArrayList<TreeElem>(0);
			} else {
				Term subType = ((TermMore) (termType)).getSub();
				for (int i = 0; i < e.dotPos; ++i)
					types.add(subType);
				break;
			}
		}
		case token:
		case epsilon:
			return new ArrayList<TreeElem>(0);
		case reference: {
			List<TreeElem> ret;
			ret = findElements(lastChildType(e), e.from, e.getGroup(), e
					.getBuffer());
			// Possible to select first match and go on without problem.
//			while(ret.size()>1)
//			{
//				ret.remove(ret.size()-1);
//			}
			if (ret.size() != 1) {
				System.err.println(""+ret);
//				System.out.println(e.getBuffer().print());
				ret = findElements(lastChildType(e), e.from, e.getGroup(), e
						.getBuffer());
				throw createAmbigousException(types, e.from, e.getGroup(), e
						.getBuffer(), ret);
				// new ParseException("parse result is ambiguos!" + e);
			}
			return ret;
		}
		default:
			throw new RuntimeException("Internal error:Unknown term type");
		}
		List<TreeElem> ret = findElements(types, e.from, e.getGroup(), e
				.getBuffer());
		// if(ret.size()!=1)
		// throw new ParseException("parse result is ambiguos!"+e);
		return ret;
	}

	private String getString(TreeElem elem)
	{
		List<IToken> toks=elem.getBuffer().getTokens();
		int from=elem.from;
		int to=elem.getGroup();
		IToken fromToken=toks.get(from);
		return fromToken.getSource().getFullSequence().subSequence(from, to).toString();
	}
	private ParseException createAmbigousException(List<Term> types, int from,
			int to, ElemBuffer buf, List<TreeElem> sub) {
		IToken fromToken = buf.tokens.get(from);
		IToken toToken = buf.tokens.get(to);
		int fromPos = fromToken.getPos();
		int toPos = toToken.getPos() + toToken.getLength();
//		logger.getErr().println(buf.print());
		return new ParseException("parse ambigous from:"
				+ from
				+ " to: "
				+ to
				+ "("
				+ fromPos
				+ ";"
				+ toPos
				+ ")"
				+ "string: "
				+ fromToken.getSource().getFullSequence().subSequence(fromPos,
						toPos-1)
				+ " possible solutions: "+sub
//						+" foolowed by: "+
//						fromToken.getSource().getFullSequence().subSequence(toPos,
//								toPos+10)
								);
	}
	private ParseException createAmbigousException2(List<Term> types, int from,
			int to, ElemBuffer buf, List<List<TreeElem>> sub) {
		IToken fromToken = buf.tokens.get(from);
		IToken toToken = buf.tokens.get(to);
		int fromPos = fromToken.getPos();
		int toPos = toToken.getPos() + toToken.getLength();
//		logger.getErr().println(buf.print());
		return new ParseException("parse ambigous from:"
				+ from
				+ " to: "
				+ to
				+ "("
				+ fromPos
				+ ";"
				+ toPos
				+ ")"
				+ "string: "
				+ fromToken.getSource().getFullSequence().subSequence(fromPos,
						toPos-1)
//						+" foolowed by: "+
//						fromToken.getSource().getFullSequence().subSequence(toPos,
//								toPos+10)
								);
	}

	/**
	 * Find the given number of elements(with given type) that generate the text
	 * from from to to in the given parse result.
	 * 
	 * @param types the required type
	 * @param from
	 * @param to
	 * @param buf
	 * @return null in case the element can not be generated
	 * @throws ParseException in case of ambigous solution
	 */
	List<TreeElem> findElements(List<Term> types, int from, int to,
			ElemBuffer buf) throws ParseException {
		List<List<TreeElem>> ret = new ArrayList<List<TreeElem>>();
		if (types.size() == 1) {
			List<TreeElem> sub = (findElements(types.get(0), from, to, buf));
			if (sub.size() > 1) {
				sub = (findElements(types.get(0), from, to, buf));
				throw createAmbigousException(types, from, to, buf, sub);
			}else if(sub.size()==0)
			{
				
			}else
			{
				ret.add(sub);
			}
		} else {
			// All elements that generate the last type until the last position
			// but we don't check the from part
			List<TreeElem> items=findElements(types.get(types.size() - 1), to,
					buf);
			for (TreeElem last : items) {
				int lastFrom=last.from;
				List<Term> subTypes=types.subList(0,
						types.size() - 1);
				List<TreeElem> sub = findElements(subTypes, from, lastFrom, buf);
				if (sub != null) {
					sub.add(last);
					ret.add(sub);
				}
			}
		}
		if (ret.size() > 1)
		{
			throw createAmbigousException2(types, from, to, buf, ret);
		}
		if (ret.size() == 0)
		{
			return null;
		}
		return ret.get(0);
	}

	private ParseException createCannotParseException(List<Term> types, int from,
			int to, ElemBuffer buf) {
		IToken fromToken = buf.tokens.get(from);
		IToken toToken = buf.tokens.get(to);
		int fromPos = fromToken.getPos();
		int toPos = toToken.getPos() + toToken.getLength();
		logger.println(buf.print());
		return new ParseException("cannot parse to type: "+types+" from:"
				+ from
				+ " to: "
				+ to
				+ "("
				+ fromPos
				+ ";"
				+ toPos
				+ ")"
				+ "string: "
				+ fromToken.getSource().getFullSequence().subSequence(fromPos,
						toPos-1)
//						+" foolowed by: "+
//						fromToken.getSource().getFullSequence().subSequence(toPos,
//								toPos+10)
								);
	}

	/**
	 * Find elements that generate the specified term type to the given position
	 * in the given parse result.
	 * 
	 * @param type
	 * @param to
	 * @param buf
	 * @return
	 */
	private List<TreeElem> findElements(Term type, int to, ElemBuffer buf) {
		List<TreeElem> ret = new ArrayList<TreeElem>();
		int fromIndex=buf.getGroupStart(to);
		int toIndex=buf.getGroupEnd(to);
		for(int i=fromIndex; i<toIndex;++i)
		{
			if (buf.getTermTypeId(i)==type.getId() && GenerationRules.isPassed(buf, i)) {
				ret.add(new TreeElem(buf, i, to));
			}
		}
		return ret;
	}

	private List<TreeElem> findElements(Term type, int from, int to, ElemBuffer buf) {
		List<TreeElem> ret = new ArrayList<TreeElem>();
		for (TreeElem e : findElements(type, to, buf)) {
			if (e.from == from)
				ret.add(e);
		}
		return ret;
	}

	private Term lastChildType(TreeElem e) {
		Term termType=e.getType();
		switch (termType.getType()) {
		case and: {
			int i = e.dotPos;
			if (i <= 0) {
				return null;
			} else {
				TermAnd and = (TermAnd) termType;
				return and.getSubs().get(e.dotPos - 1);
			}
		}
		case or: {
			TermOr or = (TermOr) termType;
			return or.getSubs().get(e.choice);
		}
		case epsilon: {
			return null;
		}
		case oneormore:
		case zeroormore: {
			if (e.dotPos > 0) {
				return ((TermMore) termType).getSub();
			}
		}
		case reference: {
			return ((TermRef) termType).getSub();
		}
		case token: {
			return null;
		}
		}
		return null;
	}

}
