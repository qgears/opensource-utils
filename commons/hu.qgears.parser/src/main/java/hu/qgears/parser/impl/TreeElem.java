package hu.qgears.parser.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.impl.TextSource;


/**
 * Implementation of a parse result node.
 */
final public class TreeElem implements ITreeElem {
	protected int dotPos;
	protected int choice;
	private int typeId;
	/**
	 * Group index where this element produces tokens.
	 */
	protected int from;
	private int group;
	private Term type;
	private TokenArray tokens;
	private ElemBuffer buffer;
	private TextSource textSource;
	private int textIndexFrom;
	private int textIndexTo;
	private boolean isToken;

	@SuppressWarnings("unchecked")
	List<TreeElem> subs = Collections.EMPTY_LIST;

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

	public int getGroup() {
		return group;
	}

	/**
	 * The type of this element in the parse tree.
	 * @return
	 */
	public Term getType() {
		return type;
	}

	public String getTypeName() {
		return getType().getName();
	}

	@Override
	public int getTextIndexFrom()
	{
		return textIndexFrom;
	}
	@Override
	public int getTextIndexTo()
	{
		return textIndexTo;
	}
	public String getString() {
		int f = getTextIndexFrom();
		int t = getTextIndexTo();
		if(t<f)
		{
			return "";
		}
		return textSource.firstChars(f, t - f);
	}

	public TreeElem(ElemBuffer buffer, int dotPos, int typeId, int choice, int from, int group) {
		super();
		this.buffer = buffer;
		this.dotPos=dotPos;
		this.typeId=typeId;
		this.choice=choice;
		this.from=from;
		this.group = group;
		textSource=buffer.getSource();
		type=buffer.resolve(typeId);
		isToken=EType.token.equals(getType().getType());
		textIndexFrom=buffer.getTokens().pos(from);
		tokens=buffer.getTokens();
		if(group==0)
		{
			textIndexTo=0;
		}else
		{
			int lastTokenIndex=group -1;
			int pos = tokens.pos(lastTokenIndex);
			int l = tokens.length(lastTokenIndex);
			textIndexTo = pos + l;
		}
	}

	public TreeElem(ElemBuffer buf, int absoluteIndex, int group) {
		this(buf, buf.getDotPosition(absoluteIndex), buf.getTermTypeId(absoluteIndex),
				buf.getChoice(absoluteIndex), buf.getFrom(absoluteIndex), group);
	}

	@Override
	public String toString() {
		if(buffer!=null)
		{
			return ""+getTypeName()+" at: " + group + " " + buffer.toString(dotPos, typeId, choice, from) + " "
					+ (tokens.toString(from, group));
		}else
		{
			return ""+getTypeName()+" at: " + group + " "
					+ tokens.toString(from, group);
		}
	}

	private static final int maxKeysSimple=3;
	/**
	 * User objects storage map implemented as an array when small to spare RAM.
	 */
	private Object userObjects = null;
	@Override
	public void setUserObject(String key, Object value) {
		if(userObjects instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> map=(Map<String, Object>)userObjects;
			map.put(key, value);
		}else if(userObjects instanceof Object[])
		{
			Object[] arr=(Object[]) userObjects;
			int l=arr.length/2;
			for(int i=0;i<l;++i)
			{
				if(Objects.equals(key, arr[i*2]))
				{
					arr[i*2+1]=value;
					return;
				}
			}
			if(l==maxKeysSimple)
			{
				Map<String, Object> map=new HashMap<>();
				for(int i=0;i<l;++i)
				{
					map.put((String)arr[i*2], arr[i*2+1]);
				}
				map.put(key, value);
				userObjects=map;
			}else
			{
				Object[] newArr=Arrays.copyOf(arr, arr.length+2);
				newArr[l*2]=key;
				newArr[l*2+1]=value;
				userObjects=newArr;
			}
		}else
		{
			Object[] arr=new Object[2];
			userObjects=arr;
			arr[0] = key;
			arr[1] = value;
		}
	}

	@Override
	public Object getUserObject(String key) {
		if(userObjects instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> map=(Map<String, Object>)userObjects;
			return map.get(key);
		}else if(userObjects instanceof Object[])
		{
			Object[] arr=(Object[]) userObjects;
			int l=arr.length/2;
			for(int i=0;i<l;++i)
			{
				if(key==null?arr[i*2]==null:key.equals(arr[i*2]))
				{
					return arr[i*2+1];
				}
			}
			return null;
		}else
		{
			return null;
		}
	}
	@Override
	public void stripParseDataRecursive() {
		buffer=null;
		for(TreeElem te: subs)
		{
			te.stripParseDataRecursive();
		}
	}

	public String getSourceString() {
		return tokens.getSource().substring(textIndexFrom, textIndexTo).toString();
	}

	public boolean isToken() {
		return isToken;
	}
}
