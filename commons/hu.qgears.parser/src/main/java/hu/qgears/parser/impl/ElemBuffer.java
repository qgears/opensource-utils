package hu.qgears.parser.impl;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.util.UtilIntArrayFlexible;



/**
 * The element buffer of the early algorithm.
 */
final public class ElemBuffer {
	private long nanosStoredAlready=0;
	static final private int nBitHash=16;
	final private static int hashMask=(1<<nBitHash)-1;
	private static final int hashEntrySize=2;
	int[] hashTable=new int[(1<<nBitHash)*hashEntrySize];
	final public UtilIntArrayFlexible elems = new UtilIntArrayFlexible();
	final public UtilIntArrayFlexible genBys = new UtilIntArrayFlexible();
	final private UtilIntArrayFlexible groupStarts = new UtilIntArrayFlexible();
	private static final int offsetType=1;
	private static final int offsetDotPos=0;
	private static final int offsetFrom=3;
	private static final int offsetChoice=2;
	private static final int offsetGeneratedBy=4;
	private static final int offsetGroup=5;
	private static final int offsetNextHashEntry=6;
	private static final int storeLength=7;
	private static final int hashOffsetGroup=0;
	private static final int hashOffsetEntry=1;

	/**
	 * The list of tokens to be parsed. Constant.
	 */
	protected List<IToken> tokens;
	/**
	 * Position counted in the number of stored elements.
	 */
	private int pos = 0;
	private int nGenBy=0;
	private ILanguage lang;
	private int currentGroup = -1;
	private int currentGroupStart=0;
	private Term[] terms = new Term[0];
	public long nanosDoGenerates;
	/**
	 * Index of the current group.
	 * @return
	 */
	public int getCurrentGroup() {
		return currentGroup;
	}

	int getGroupSize(int grId) {
		int ret;
		if (grId < currentGroup) {
			ret = groupStarts.get(grId + 1) - groupStarts.get(grId);
		} else {
			ret = pos - groupStarts.get(currentGroup);
		}
		return ret;
	}

	public ElemBuffer() {
	}
	/**
	 * Clear all stored data in this parsing buffer.
	 * @param terms array of rules of the language - used to find a rule by its index
	 * @param tokens the text to be parsed as a list of tokens - output of the tokenizer
	 * @param lang - reference to the language itself that is currently used to parse the text.
	 */
	public void reInit(Term[] terms, List<IToken> tokens, ILanguage lang)
	{
		nanosStoredAlready=0;
		nanosDoGenerates=0;
		elems.clear();
		groupStarts.clear();
		currentGroup=-1;
		currentGroupStart=0;
		nGenBy=0;
		for(int i=0;i<hashTable.length;++i)
		{
			hashTable[i]=-1;
		}
		this.terms=terms;
		this.tokens=tokens;
		this.lang=lang;
		newGroup();
	}

	/**
	 * 
	 * @param dotPos
	 * @param type
	 * @param choice
	 * @param from
	 * @param generatedByAbsolute index of the element that generates this element in the from group.
	 * @return
	 */
	public int addElement(int dotPos, int type, int choice, int from, int generatedByAbsolute) {
		int idx=storeadAlready(dotPos, type, choice, from);
		int ret=0;
		if (idx<0) {
			save(elems, pos, dotPos, type, choice, from);
			idx=pos;
			pos ++;
			ret++;
		}
		markGeneratedBy(idx, generatedByAbsolute);
		return ret;
	}
	private static final int genbyEntrySize=2;
	private void markGeneratedBy(int idx, int generatedByAbsolute) {
		UtilIntArrayFlexible toWrite=elems;
		int ptrIdx=idx*storeLength+offsetGeneratedBy;
		int nextPtrIndex;
		while((nextPtrIndex=toWrite.get(ptrIdx))!=-1)
		{
			toWrite=genBys;
			ptrIdx=nextPtrIndex*genbyEntrySize+1;
			if(genBys.get(nextPtrIndex*genbyEntrySize)==generatedByAbsolute)
			{
				// Do not add the same value twice!
				return;
			}
		}
		genBys.set(nGenBy*genbyEntrySize, generatedByAbsolute);
		genBys.set(nGenBy*genbyEntrySize+1, -1);
		toWrite.set(ptrIdx, nGenBy);
		nGenBy++;
	}
	public Set<Integer> getGeneratedBy(int absoluteIndex) {
		Set<Integer> ret=new HashSet<Integer>();
		int ptrIdxSrc=elems.get(absoluteIndex*storeLength+offsetGeneratedBy);
		while(ptrIdxSrc!=-1)
		{
			int generatedBy=genBys.get(ptrIdxSrc*genbyEntrySize);
			ret.add(generatedBy);
			ptrIdxSrc=genBys.get(ptrIdxSrc*genbyEntrySize+1);
		}
		return ret;
	}
	public void iterateGeneratedByAddElementCopyGenerator(int absoluteIndex) {
		int ptrIdxSrc=elems.get(absoluteIndex*storeLength+offsetGeneratedBy);
		while(ptrIdxSrc!=-1)
		{
			int i=genBys.get(ptrIdxSrc*genbyEntrySize);
			if(i>=0)
			{
				addElementCopyGenerator(getDotPosition(i)+1, getTermTypeId(i),
					getChoice(i), getFrom(i), i);
			}
			ptrIdxSrc=genBys.get(ptrIdxSrc*genbyEntrySize+1);
		}
	}

	private void copyGeneratedBy(int idx, int copyGeneratedByAbsolute) {
		int ptrIdxSrc=elems.get(copyGeneratedByAbsolute*storeLength+offsetGeneratedBy);
		while(ptrIdxSrc!=-1)
		{
			int generatedBy=genBys.get(ptrIdxSrc*genbyEntrySize);
			markGeneratedBy(idx, generatedBy);
			ptrIdxSrc=genBys.get(ptrIdxSrc*genbyEntrySize+1);
		}
	}

	/**
	 * 
	 * @param dotPos
	 * @param type
	 * @param choice
	 * @param from
	 * @param copyGeneratedByAbsolute copy the generated by list of the element at this index.
	 * @return
	 */
	public int addElementCopyGenerator(int dotPos, int type, int choice, int from, int copyGeneratedByAbsolute) {
		int idx=storeadAlready(dotPos, type, choice, from);
		int ret=0;
		if (idx<0) {
			save(elems, pos, dotPos, type, choice, from);
			idx=pos;
			pos ++;
			ret++;
		}
		copyGeneratedBy(idx, copyGeneratedByAbsolute);
		return ret;
	}

	/**
	 * Is this element already stored?
	 * @param dotPos
	 * @param type
	 * @param choice
	 * @param from
	 * @return the absolute index of the storage of the element.
	 */
	private int storeadAlready(int dotPos, int type, int choice, int from) {
		nanosStoredAlready-=System.nanoTime();
		int hash=hashCode(dotPos, type, choice, from);
		int hg=hashTable[hash*hashEntrySize+hashOffsetGroup];
		if(hg==currentGroup)
		{
			int i=hashTable[hash*hashEntrySize+hashOffsetEntry];
			while(i>=0)
			{
				if(isEq(i, dotPos, type, choice, from))
				{
					nanosStoredAlready+=System.nanoTime();
					return i;
				}
				i=getNextHashEntry(i);
			}
		}
//		for(int i=currentGroupStart; i<pos;++i)
//		{
//			if(isEq(i, dotPos, type, choice, from))
//			{
//				nanosStoredAlready+=System.nanoTime();
//				return i;
//			}
//		}
		nanosStoredAlready+=System.nanoTime();
		return -1;
	}
	private int hashCode(int dotPos, int type, int choice, int from)
	{
		return (dotPos^(type<<10)^(choice<<5)^(from))&hashMask;
	}

	public ElemBuffer newGroup() {
		currentGroup++;
		currentGroupStart=pos;
		groupStarts.set(currentGroup, pos);
		return this;
	}

	public int getSize() {
		return pos;
	}
	public int getGroupStart(int groupId)
	{
		return groupStarts.get(groupId);
	}
	public int getGroupEnd(int groupId)
	{
		if (groupId < currentGroup) {
			return groupStarts.get(groupId + 1);
		} else {
			return pos;
		}
	}

	final public Term resolve(int termTypeId) {
		return terms[termTypeId];
	}

	public String print() {
		StringBuilder ret = new StringBuilder();
		for (int j = 0; j <= currentGroup; ++j) {
			ret.append("group " + j + ":\n");
			for(int i=getGroupStart(j); i<getGroupEnd(j);++i)
			{
				ret.append(toString(i) + "\n");
			}
			ret.append("\n");
		}
		return ret.toString();
	}

	public List<IToken> getTokens() {
		return tokens;
	}

	IToken getTokenOfGroup(int grp) {
		return tokens.get(grp);
	}

	public Term[] getTerms() {
		return terms;
	}

	public ILanguage getLang() {
		return lang;
	}

	/**
	 * Get the text source that this buffer is defined on.
	 * 
	 * Convenient method for: tokens.get(0).getSource()
	 * 
	 * @return the text source that this buffer is defined on.
	 */
	public ITextSource getSource() {
		return tokens.get(0).getSource();
	}

	public int getCurrentGroupEnd() {
		return pos;
	}
	public int getCurrentGroupStart()
	{
		return currentGroupStart;
	}
	/**
	 * Does the buffer contain the requested element?
	 * @param fromIndex index including
	 * @param toIndex not including
	 * @param e
	 * @return
	 */
	public boolean contains(int fromIndex, int toIndex, int dotPos, int type, int choice, int from)
	{
		for(int i=fromIndex;i<toIndex;++i)
		{
			if(isEq(i, dotPos, type, choice, from))
			{
				return true;
			}
		}
		return false;
	}

	public void printCurrentGroup(PrintStream err) {
		for(int i=currentGroupStart; i<pos; ++i)
		{
			err.println(toString(i));
		}
	}
	public long getNanosStoredAlready() {
		return nanosStoredAlready;
	}

	public void save(UtilIntArrayFlexible arr, int absoluteIndex, int dotPos, int type, int choice, int from) {
		int pos=absoluteIndex*storeLength;
		int hash=hashCode(dotPos, type, choice, from);
		int hg=hashTable[hash*hashEntrySize+hashOffsetGroup];
		int next=-1;
		if(hg!=currentGroup)
		{
			hashTable[hash*hashEntrySize+hashOffsetGroup]=currentGroup;
			hashTable[hash*hashEntrySize+hashOffsetEntry]=absoluteIndex;
		}else
		{
			next=hashTable[hash*hashEntrySize+hashOffsetEntry];
			hashTable[hash*hashEntrySize+hashOffsetEntry]=absoluteIndex;
		}
		arr.set(pos+offsetDotPos, dotPos).set(pos + offsetType, type).set(pos + offsetChoice,
				choice).set(pos + offsetFrom, from).set(pos+offsetGeneratedBy, -1)
				.set(pos+offsetGroup, currentGroup).set(pos+offsetNextHashEntry, next);
	}

	public String toString(int dotPos, int type, int choice, int from) {
		Term termType=resolve(type);
		return termType.getName() + ":" + termType.getType() + " dotPos:"
				+ dotPos + " choice:" + choice + " from: " + from + " "
				+ (GenerationRules.isPassed(termType, dotPos) ? "PASSED" : "");
	}
	public String toString(int absoluteIndex) {
		Term termType=resolve(getTermTypeId(absoluteIndex));
		return termType.getName() + "("+getTermTypeId(absoluteIndex)+"): group/absIndex: "+getGroup(absoluteIndex)+"/"+absoluteIndex+": " + termType.getType() + " dotPos:"
				+ getDotPosition(absoluteIndex) + " choice:" + 
				getChoice(absoluteIndex) + " from: " + getFrom(absoluteIndex) + " "
				+ (GenerationRules.isPassed(termType, getDotPosition(absoluteIndex)) ? "PASSED" : "");
	}

	public int getTermTypeId(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetType);
	}

	public int getDotPosition(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetDotPos);
	}

	public int getFrom(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetFrom);
	}
	public int getGroup(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetGroup);
	}

	public int getChoice(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetChoice);
	}
	public int getNextHashEntry(int absoluteIndex) {
		return elems.get(absoluteIndex*storeLength+offsetNextHashEntry);
	}

	public boolean isEq(int absoluteIndex, int dotPos, int type, int choice, int from) {
		int pos=absoluteIndex*storeLength;
		return elems.get(pos+offsetDotPos)==dotPos && elems.get(pos+offsetType)==type &&
				elems.get(pos+offsetChoice)==choice && elems.get(pos+offsetFrom)==from;
	}
}
