package hu.qgears.commons;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of <code>MultiMap</code>
 * based on <code>HashMap</code> mapped to HashSet.
 * @author rizsi
 *
 * @param <K>
 * @param <V>
 */
public class MultiMapHashToHashSetImpl<K, V> extends HashMap<K, Collection<V>> implements MultiMap<K, V>{

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	@Override
	public HashSet<V> get(Object key) {
		HashSet<V> ret=(HashSet<V>)super.get(key);
		if(ret==null)
		{
			ret=new HashSet<V>();
			put((K)key, ret);
		}
		return ret;
	}
	@Override
	public void putSingle(K key, V value)
	{
		HashSet<V> list=get(key);
		list.add(value);
	}
	@Override
	public void removeSingle(K key, V value) {
		HashSet<V> list=get(key);
		list.remove(value);
		if(list.size()==0)
		{
			remove(key);
		}
	}
	@Override
	public HashSet<V> getPossibleNull(K key) {
		HashSet<V> ret=(HashSet<V>)super.get(key);
		return ret;
	}
	public HashSet<V> getPossibleDefault(String key, HashSet<V> def) {
		HashSet<V> ret=(HashSet<V>)super.get(key);
		if(ret==null)
		{
			ret=def;
		}
		return ret;
	}
}
