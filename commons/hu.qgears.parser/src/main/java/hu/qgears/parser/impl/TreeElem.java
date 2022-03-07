package hu.qgears.parser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.IToken;


/**
 * Implementation of a parse result node.
 */
public class TreeElem implements ITreeElem {
	protected int dotPos;
	protected int choice;
	private int typeId;
	/**
	 * Group index where this element produces tokens.
	 */
	protected int from;
	private int group;
	private ElemBuffer buffer;
	List<TreeElem> subs = new ArrayList<TreeElem>();

	public List<TreeElem> getSubs() {
		return subs;
	}

	public void setSubs(List<TreeElem> subs) {
		this.subs = subs;
	}

	/**
	 * The early parse buffer.
	 * @return
	 */
	public ElemBuffer getBuffer() {
		return buffer;
	}

//	/**
//	 * The corresponding element in the early parse tables.
//	 * @return
//	 */
//	public Elem getElem() {
//		return elem;
//	}

	public int getGroup() {
		return group;
	}

	/**
	 * The type of this element in the parse tree.
	 * @return
	 */
	public Term getType() {
		return buffer.resolve(typeId);
	}

	public IToken getToken() {
		if (EType.token.equals(getType().getType())) {
			return buffer.getTokenOfGroup(from);
		}
		return null;
	}

	public String getTypeName() {
		return getType().getName();
	}

	@Override
	public int getTextIndexFrom()
	{
		int f = getBuffer().getTokenOfGroup(from).getPos();
		return f;
	}
	@Override
	public int getTextIndexTo()
	{
		if(group==0)
		{
			return 0;
		}
		IToken tok = getBuffer().getTokenOfGroup(group - 1);
		int t = tok.getPos() + tok.getLength();
		return t;
	}
	public String getString() {
		int f = getTextIndexFrom();
		int t = getTextIndexTo();
		if(t<f)
		{
			return "";
		}
		return getBuffer().getSource().firstChars(f, t - f);
	}

	public TreeElem(ElemBuffer buffer, int dotPos, int typeId, int choice, int from, int group) {
		super();
		this.buffer = buffer;
		this.dotPos=dotPos;
		this.typeId=typeId;
		this.choice=choice;
		this.from=from;
		this.group = group;
	}

	public TreeElem(ElemBuffer buf, int absoluteIndex, int group) {
		this(buf, buf.getDotPosition(absoluteIndex), buf.getTermTypeId(absoluteIndex),
				buf.getChoice(absoluteIndex), buf.getFrom(absoluteIndex), group);
	}

	@Override
	public String toString() {
		IToken tok = getToken();
		return ""+getTypeName()+" at: " + group + " " + buffer.toString(dotPos, typeId, choice, from) + " "
				+ (tok == null ? "" : "" + tok);
	}

	private Map<String, Object> userObjects=new HashMap<String, Object>();
	@Override
	public void setUserObject(String key, Object value) {
		userObjects.put(key, value);
	}

	@Override
	public Object getUserObject(String key) {
		return userObjects.get(key);
	}
}
