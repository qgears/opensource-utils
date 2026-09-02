package hu.qgears.commons;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * DTO for a list of objects.
 * equals and hashCode is overridden in a way that it can be used in hashmaps as complex key.
 */
public class NObject <T>
{
	private T[] data;
	private boolean hashInited;
	private int hashCode;
	@SafeVarargs
	public NObject(T... a) {
		super();
		this.data = a;
	}
	public NObject(List<T> a, T[] baseType) {
		super();
		this.data = Arrays.copyOf(baseType, a.size());
		for(int i=0;i<a.size();++i)
		{
			data[i]=a.get(i);
		}
	}
	/**
	 * Returns the internal array so it has to be handled read only.
	 * @return
	 */
	public T[] getData() {
		return data;
	}
	@Override
	public int hashCode() {
		if(!hashInited)
		{
			hashInited=true;
			hashCode=0;
			for(Object o: data)
			{
				hashCode^=h(o);
			}
		}
		return hashCode;
	}
	private int h(Object o) {
		if(o==null)
		{
			return 0;
		}
		return o.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NObject<?>)
		{
			NObject<?> other=(NObject<?>) obj;
			if(other.data.length==data.length)
			{
				for(int i=0;i<data.length;++i)
				{
					if(!UtilEquals.safeEquals(other.data[i], data[i]))
					{
						return false;
					}
				}
				return true;
			}else
			{
				return false;
			}
		}
		return super.equals(obj);
	}
	@Override
	public String toString() {
		StringBuilder ret=new StringBuilder();
		ret.append("[");
		for(int i=0;i<data.length;++i)
		{
			if(i>0)
			{
				ret.append(',');
			}
			ret.append(""+data[i]);
		}
		ret.append("]");
		return ret.toString();
	}
	public static <T extends Comparable<T>> Comparator<NObject<T>> comparator(int... order)
	{
		return new Comparator<NObject<T>>() {

			@Override
			public int compare(NObject<T> o1, NObject<T> o2) {
				for(int index: order)
				{
					T t1=o1.data[index];
					T t2=o2.data[index];
					int cmp=t1.compareTo(t2);
					if(cmp!=0)
					{
						return cmp;
					}
				}
				return 0;
			}
		};
	}
}
