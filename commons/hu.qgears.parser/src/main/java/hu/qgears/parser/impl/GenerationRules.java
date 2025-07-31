package hu.qgears.parser.impl;

import java.util.Set;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermAnd;
import hu.qgears.parser.language.impl.TermMore;
import hu.qgears.parser.language.impl.TermOr;
import hu.qgears.parser.language.impl.TermRef;
import hu.qgears.parser.language.impl.TermToken;
import hu.qgears.parser.tokenizer.TokenArray;


/**
 * Rules to generate new elements - implementing early parser.
 * @author rizsi
 *
 */
public class GenerationRules {
	/**
	 * TODO false branch should be deleted.
	 */
	static boolean optimizeFindGenerators=true;

	private GenerationRules(){}
	/**
	 * Generate elements onto the same group by finding all finished elements
	 * and looking up their creator elements and adding the followants of the creator elements.
	 * @param indexWithinGroup The index of the element within the group to try to generate its descendants
	 * @param at We are at this group index currently
	 * @param eb elements are generated into this buffer
	 * @param nextToken
	 */
	public static final void generateOnSameGroup(int absoluteIndex, TokenArray tokens, int at, ElemBuffer eb) {
		int typeId=eb.getTermTypeId(absoluteIndex);
		Term term = eb.resolve(typeId);
		boolean isPassed = isPassed(eb, absoluteIndex, term);
		if (isPassed) {
			generatePassed(absoluteIndex, at, eb, term);
		}
		Term sub = getSub(eb, absoluteIndex, isPassed);
		if (sub != null) {
			generateNonTerm(eb, sub, tokens, at, absoluteIndex);
		}
	}
	/** Generate elements onto the same group by finding all finished elements
	 * and looking up their creator elements and adding the followants of the creator elements.
	 * @param indexWithinGroup The index of the element within the group to try to generate its descendants
	 * @param at We are at this group index currently
	 * @param eb elements are generated into this buffer
	 * @param collectNontermOutput 
	 * @param nextToken
	 */
	public static final void generateOnSameGroupForAccelerator(int absoluteIndex, int at, ElemBuffer eb, Set<TermToken> collectNontermOutput) {
		int typeId=eb.getTermTypeId(absoluteIndex);
		Term term = eb.resolve(typeId);
		boolean isPassed = isPassed(eb, absoluteIndex, term);
		if (isPassed) {
			generatePassed(absoluteIndex, at, eb, term);
		}
		Term sub = getSub(eb, absoluteIndex, isPassed);
		if (sub != null) {
			generateNonTermForAccelerator(eb, sub, at, absoluteIndex, collectNontermOutput);
		}
	}
	/**
	 * Get the term that can be added to the same group in this state.
	 * 
	 * @param e
	 * @param isPassed
	 * @return
	 */
	static private Term getSub(ElemBuffer eb, int absoluteIndex, boolean isPassed) {
		Term term = eb.resolve(eb.getTermTypeId(absoluteIndex));
		EType type = term.getType();
		switch (type) {
		case and: {
			TermAnd termO = (TermAnd) term;
			if (!isPassed) {
				return termO.getSubs().get(eb.getDotPosition(absoluteIndex));
			}
			return null;
		}
		case or: {
			if (!isPassed) {
				TermOr termO = (TermOr) term;
				return termO.getSubs().get(eb.getChoice(absoluteIndex));
			}
			return null;
		}
		case oneormore:
		case zeroormore: {
			return ((TermMore) term).getSub();
		}
		case reference: {
			if (!isPassed) {
				return ((TermRef) term).getSub();
			}
		}
		case epsilon:
		case token: {
			return null;
		}
		default:
			throw new RuntimeException();
		}

	}

	/**
	 * True if the element pointed in the buffer was passed so it is a valid match.
	 * @param eb
	 * @param absoluteIndex
	 * @return
	 */
	static public boolean isPassed(ElemBuffer eb, int absoluteIndex) {
		Term term=eb.resolve(eb.getTermTypeId(absoluteIndex));
		return isPassed(eb, absoluteIndex, term);
	}
	/**
	 * Is this element passed at this state?
	 * 
	 * @param eb
	 * @param absoluteIndex
	 * @param term - redundant information but it is optimized to not look up multiple times.
	 * @return
	 */
	static private boolean isPassed(ElemBuffer eb, int absoluteIndex, Term term) {
		return isPassed(term, eb.getDotPosition(absoluteIndex));
	}
	/**
	 * Is this element passed at this state?
	 * 
	 * @param eb
	 * @param absoluteIndex
	 * @param term - redundant information but it is optimized to not look up multiple times.
	 * @return
	 */
	static public boolean isPassed(Term term, int dotPos) {
		EType type = term.getType();
		switch (type) {
		case and: {
			TermAnd termO = (TermAnd) term;
			return dotPos >= termO.getSubs().size();
		}
		case or:
		case token:
		case reference:
		case oneormore: {
			return dotPos > 0;
		}
		case epsilon:
		case zeroormore: {
			return true;
		}
		default:
			throw new RuntimeException();
		}
	}

	/**
	 * Return elements that are generated on the next group by this rule.
	 * @param buffer buffer to add the generated elements to 
	 * @param absoluteIndex absolute index of the elem to generate its descendants.
	 * @param at
	 * @param token
	 * @return the number of elements added
	 */
	static public int generateOnNextGroup(ElemBuffer buffer, int absoluteIndex, TokenArray tokens, int at) {
		int typeId=buffer.getTermTypeId(absoluteIndex);
		Term term = buffer.resolve(typeId);
		EType type = term.getType();
		switch (type) {
		case token: {
			int dotPos=buffer.getDotPosition(absoluteIndex);
			if (dotPos== 0) {
				TermToken tt = (TermToken) term;
				if (tt.getTokenType().getId() == tokens.type(at)) {
					// Token is found in source code.
					return buffer.addElementCopyGenerator(dotPos+1, typeId, buffer.getChoice(absoluteIndex), buffer.getFrom(absoluteIndex), absoluteIndex);
				} else {
					return 0;
				}
			}
		}
		default:
			return 0;
		}
	}

	/**
	 * Find the elements on from group that generate this element on current group
	 * and add then to the buffer with the dot passed one.
	 * @param absoluteIndex index of passed element to process.
	 * @param at current group index (unused)
	 * @param eb buffer
	 * @param termType passed symbol type
	 * @return
	 */
	static private void generatePassed(int absoluteIndex, int at, ElemBuffer eb, Term termType) {
		// The elem that is the source of this element must be present on the
		// group where this element is from.
		// That element's all parents (which has generated this element)
		// Must be inserted in this group with one dot position shifted.
		// int from=elem.from;
		// List<Elem> elemsOnFrom=eb.getGroup(from);
		if(optimizeFindGenerators)
		{
			eb.iterateGeneratedByAddElementCopyGenerator(absoluteIndex);
		}else
		{
			eb.nanosDoGenerates-=System.nanoTime();
			int from = eb.getFrom(absoluteIndex);
			int idfrom = eb.getGroupStart(from);
			int to=eb.getGroupEnd(from);
			Set<Integer> s=eb.getGeneratedBy(absoluteIndex); 
			for (int i=idfrom; i<to;++i)
			{
				if (generates(eb, i, termType)) {
					if(!s.remove(i))
					{
						throw new RuntimeException("Not Generated by: "+s+" "+eb.toString(i)+" this: "+eb.toString(absoluteIndex));
					}
					eb.addElementCopyGenerator(eb.getDotPosition(i)+1, eb.getTermTypeId(i),
							eb.getChoice(i), eb.getFrom(i), i);
				}
			}
			s.remove(-1);
			if(s.size()>0)
			{
				throw new RuntimeException("Generated by: "+s+" "+eb.toString(s.iterator().next())+" this: "+eb.toString(absoluteIndex));
			}
			eb.nanosDoGenerates+=System.nanoTime();
		}
	}
	/**
	 * Check whether the given term may generate the given termtype on the same
	 * group. The inverse of the generateonSameGroup method.
	 * 
	 * @param e
	 * @param tt
	 * @return
	 */
	static public boolean generates(ElemBuffer eb, int absoluteIndex, Term tt) {
		Term tt1 = getSub(eb, absoluteIndex, isPassed(eb, absoluteIndex));
		if (tt1 == null)
			return false;
		if (tt1 == null || tt == null) {
			throw new RuntimeException("internal error!");
		}
		return tt1.getId() == tt.getId();
	}

	/**
	 * Generate all element that are generated by this term at this point.
	 * 
	 * @param term
	 * @param from
	 * @param nextToken
	 * @return
	 */
	public static final void generateNonTerm(ElemBuffer eb, Term term, TokenArray tokens, int from, int generatedBy) {
		switch (term.getType()) {
		case or: {
			TermOr termO = (TermOr) term;
			for (int ctr = 0; ctr < termO.getSubs().size(); ++ctr) {
				eb.addElement(0, term.getId(), ctr, from, generatedBy);
			}
			break;
		}
		case token: {
			TermToken tt = (TermToken) term;
			if (matches(tt, tokens, from)) {
				eb.addElement(0, term.getId(), 0, from, generatedBy);
			}
			break;
		}
		default: {
			eb.addElement(0, term.getId(), 0, from, generatedBy);
			break;
		}
		}
	}
	public static final void generateNonTermForAccelerator(ElemBuffer eb, Term term, int from, int generatedBy, Set<TermToken> collectNontermOutput) {
		switch (term.getType()) {
		case or: {
			TermOr termO = (TermOr) term;
			for (int ctr = 0; ctr < termO.getSubs().size(); ++ctr) {
				eb.addElement(0, term.getId(), ctr, from, generatedBy);
			}
			break;
		}
		case token: {
			TermToken tt = (TermToken) term;
			collectNontermOutput.add(tt);
//			// TODO mark nonterm filter rule!
//			eb.addElement(0, term.getId(), 0, from, generatedBy);
			break;
		}
		default: {
			eb.addElement(0, term.getId(), 0, from, generatedBy);
			break;
		}
		}
	}

	static private boolean matches(TermToken tt, TokenArray tokens, int at) {
		ITokenType tokenType=tt.getTokenType();
		boolean idMatch=tokenType.getId() == tokens.type(at);
		boolean hasRestriction=tt.getMatchingValue()!=null;
		if(idMatch&&hasRestriction)
		{
			Matcher mv=tt.getMatchingValue();
			boolean match=mv.matches(tokens, at);
			return match;
		}
		return idMatch;
	}
}
