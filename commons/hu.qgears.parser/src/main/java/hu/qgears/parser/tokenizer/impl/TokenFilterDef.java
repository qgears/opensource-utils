package hu.qgears.parser.tokenizer.impl;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hu.qgears.parser.language.ITokenType;



public class TokenFilterDef {
	private Set<String> toFilter = new TreeSet<String>();
	private int[] toFilterArray;

	public Set<String> getToFilter() {
		return toFilter;
	}

	public void setToFilter(Set<String> toFilter) {
		this.toFilter = toFilter;
	}

	public TokenFilterDef(List<String> toFilter) {
		super();
		this.toFilter.addAll(toFilter);
	}

	public void setupIds(List<ITokenType> flatTypes) {
		toFilterArray=new int[(flatTypes.size()+31)/32];
		int i=0;
		for(ITokenType tt: flatTypes)
		{
			if(toFilter.contains(tt.getName()))
			{
				int bank=i/32;
				int bit=i%32;
				int mask=1<<bit;
				int v=toFilterArray[bank];
				v|=mask;
				toFilterArray[bank]=v;
			}
			i++;
		}
	}

	public boolean contains(int type) {
		int bank=type/32;
		int bit=type%32;
		int mask=1<<bit;
		if(bank<toFilterArray.length)
		{
			int v=toFilterArray[bank];
			return (mask&v) !=0;
		}
		return false;
	}
}
