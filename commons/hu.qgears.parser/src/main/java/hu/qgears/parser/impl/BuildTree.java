package hu.qgears.parser.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.language.IAmbiguousSolver;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermAnd;
import hu.qgears.parser.language.impl.TermMore;
import hu.qgears.parser.language.impl.TermOr;
import hu.qgears.parser.language.impl.TermRef;
import hu.qgears.parser.tokenizer.Token;
import hu.qgears.parser.tokenizer.TokenArray;



/**
 * After creating the table of early parse built the AST tree by reading it up.
 */
public class BuildTree {
	IAmbiguousSolver ambiguousSolver;
	public BuildTree(IAmbiguousSolver ambiguousSolver) {
		super();
		this.ambiguousSolver=ambiguousSolver;
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
	
	@SuppressWarnings("unchecked")
	private static List<TreeElem> readonlyEmptyList()
	{
		return (List<TreeElem>)Collections.EMPTY_LIST;
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
				return readonlyEmptyList();
			} else {
				Term subType = ((TermMore) (termType)).getSub();
				List<TreeElem> ret = findSameElements(subType, e.dotPos, e.from, e.getGroup(), e
						.getBuffer());
				return ret;
			}
		}
		case token:
		case epsilon:
			return readonlyEmptyList();
		case reference: {
			List<TreeElem> ret;
			ret = findElements(lastChildType(e), e.from, e.getGroup(), e
					.getBuffer());
			// Possible to select first match and go on without problem.
//			while(ret.size()>1)
//			{
//				ret.remove(ret.size()-1);
//			}
			if (ret.size() > 1) {
				ret = solveAmbigousParse2(types, e.from, e.getGroup(), e.getBuffer(), ret);
				if(ret.size()!=1)
				{
					throw new RuntimeException();
				}
			}
			if(ret.size()==0)
			{
				throw new ParseException();
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
		TokenArray toks=elem.getBuffer().getTokens();
		int from=elem.from;
		int to=elem.getGroup();
		Token fromToken=toks.getToken(from);
		return toks.toString(from, to);
	}
	private ParseException createAmbigousException(List<Term> types, int from,
			int to, ElemBuffer buf, List<TreeElem> sub) {
		//Token fromToken = buf.tokens.get(from);
		//Token toToken = buf.tokens.get(to);
		int fromPos = buf.tokens.pos(from);
		int toPos = buf.tokens.pos(to) + buf.tokens.length(to);
//		logger.getErr().println(buf.print());
		ParseException p =  new ParseException("parse ambigous from:"
				+ from
				+ " to: "
				+ to
				+ "("
				+ fromPos
				+ ";"
				+ toPos
				+ ")"
				+ "string: "
				+ buf.tokens.toString(fromPos, toPos)
				+ " possible solutions: "+sub
//						+" foolowed by: "+
//						fromToken.getSource().getFullSequence().subSequence(toPos,
//								toPos+10)
								);
		p.setPosition(fromPos);
		return p;
	}
	/**
	 * Solve ambiguous situation.
	 * Possible solutions:
	 *  * throw exception
	 *  * find one possible solution
	 * @param types
	 * @param from
	 * @param to
	 * @param buf
	 * @param sub
	 * @return 1 possible solution
	 * @throws ParseException 
	 */
	private List<List<TreeElem>> solveAmbigousParse(List<Term> types, int from,
			int to, ElemBuffer buf, List<List<TreeElem>> sub) throws ParseException {
		int fromPos = buf.tokens.pos(from);
		int toPos = buf.tokens.pos(to) + buf.tokens.length(to);

		List<TreeElem> ret=null;
		if(ambiguousSolver!=null)
		{
			ret=ambiguousSolver.solveAmbiguousParse(types, from, to, buf, sub);
		}
		if(ret!=null)
		{
			List<List<TreeElem>> r=new ArrayList<>();
			r.add(ret);
			return r;
		}
		ParseException p = new ParseException("parse ambigous from:"
				+ from
				+ " to: "
				+ to
				+ "("
				+ fromPos
				+ ";"
				+ toPos
				+ ")"
				+ "string: "
				+ buf.tokens.toString(from, to)
//						+" foolowed by: "+
//						fromToken.getSource().getFullSequence().subSequence(toPos,
//								toPos+10)
								);
		p.setPosition(fromPos);
		throw p;
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
				sub = solveAmbigousParse2(types, from, to, buf, sub);
				if(sub.size()!=1)
				{
					throw new RuntimeException();
				}
			}
			if(sub.size()==0)
			{
				
			}else
			{
				ret.add(sub);
			}
		} else {
			// All elements that generate the last type until the last position
			// but we don't check the from part
			Term lastType=types.get(types.size() - 1);
			List<TreeElem> items=findElements(lastType, to,	buf);
			TreeElem prevSolution=null;
			for (TreeElem last : items) {
				int lastFrom=last.from;
				List<Term> subTypes=types.subList(0, types.size() - 1);
				if(lastFrom>=from)
				{
					List<TreeElem> sub = findElements(subTypes, from, lastFrom, buf);
					if (sub != null) {
						sub.add(last);
						ret.add(sub);
						prevSolution=last;
					}
				}
			}
		}
		if (ret.size() > 1)
		{
			ret=solveAmbigousParse(types, from, to, buf, ret);
			if(ret.size()!=1)
			{
				throw new RuntimeException();
			}
		}
		if (ret.size() == 0)
		{
			return null;
		}
		return ret.get(0);
	}
	/**
	 * Find the nodes that constitute an iteration rule.
	 * @param subType type of the node to be iterated
	 * @param n number of iterations
	 * @param from position where iteration starts
	 * @param to position where iteration ends
	 * @param buffer the parse result buffer
	 * @return list of parsed nodes. Null in case of no solution (impossible in theory)
	 * @throws ParseException in case of ambiguity
	 */
	private List<TreeElem> findSameElements(Term subType, int n, int from, int to, ElemBuffer buffer) throws ParseException {
		List<TreeElem> ret=new ArrayList<TreeElem>();
		for(int i=0;i<n;++i)
		{
			List<TreeElem> items=findElements(subType, to,	buffer);
			if(items.size()>1)
			{
				List<Term> types=new ArrayList<Term>();
				types.add(subType);
				items = solveAmbigousParse2(types, from, to, buffer, items);
				if(items.size()!=1)
				{
					throw new RuntimeException();
				}
			}
			if(items.size()==0)
			{
				return null;
			}
			TreeElem current=items.get(0);
			to=current.from;
			ret.add(current);
		}
		if(to!=from)
		{
			throw new ParseException("Internal error: parse failed "+to+"!="+from);
		}
		Collections.reverse(ret);
		return ret;
	}

	private List<TreeElem> solveAmbigousParse2(List<Term> types, int from, int to, ElemBuffer buffer,
			List<TreeElem> items) throws ParseException {
		List<List<TreeElem>> l = new ArrayList<>();
		for(TreeElem te: items)
		{
			List<TreeElem> ll=new ArrayList<>();
			ll.add(te);
			l.add(ll);
		}
		l =  solveAmbigousParse(types, from, to, buffer, l);
		if(l.size()!=1)
		{
			throw new RuntimeException();
		}
		items = l.get(0);
		return items;
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
	public static List<TreeElem> findElements(Term type, int to, ElemBuffer buf) {
		List<TreeElem> ret = new ArrayList<TreeElem>();
		int fromIndex=buf.getGroupStart(to);
		int toIndex=buf.getGroupEnd(to);
		for(int i=fromIndex; i<toIndex;++i)
		{
			if (buf.getTermTypeId(i)==type.getId()) {
				if(GenerationRules.isPassed(buf, i))
				{
					ret.add(new TreeElem(buf, i, to));
				}
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


	public void incompleteTree(ElemBuffer buffer, int i, Set<Integer> visited, String prefix) {
		int dotPos=buffer.getDotPosition(i);
		Term te=buffer.resolve(buffer.getTermTypeId(i));
		int groupId=buffer.getFrom(i);
		System.out.println(prefix+"Incomplete tree: "+te.getName()+" "+dotPos+" from: "+groupId);
		if(groupId==0)
		{
			System.out.println("FOUND!!!!");
		}
		int start=buffer.getGroupStart(groupId);
		int end=buffer.getGroupEnd(groupId);
		for(int j=start; j<end;++j )
		{
			if(!visited.contains(j))
			{
				if(GenerationRules.generates(buffer, j, te))
				{
					Term genBy=buffer.resolve(buffer.getTermTypeId(j));
					System.out.println(prefix+ "Generated by: "+genBy.getName()+" "+genBy);
					visited.add(j);
					try
					{
						incompleteTree(buffer, j, visited, " "+prefix);
					}finally
					{
						visited.remove(j);
					}
				}
			}
		}
	}
}
