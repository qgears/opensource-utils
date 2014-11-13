package hu.qgears.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of <code>MultiMap</code>
 * based on <code>HashMap</code>.
 * @author rizsi
 *
 * @param <K>
 * @param <V>
 */
public class MultiMapHashImpl<K, V> extends HashMap<K, Collection<V>> implements MultiMap<K, V>{

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<V> get(Object key) {
		List<V> ret=(List<V>)super.get(key);
		if(ret==null)
		{
			ret=new ArrayList<V>();
			put((K)key, ret);
		}
		return ret;
	}
	@Override
	public void putSingle(K key, V value)
	{
		List<V> list=get(key);
		list.add(value);
	}
	@Override
	public void removeSingle(K key, V value) {
		List<V> list=get(key);
		list.remove(value);
		if(list.size()==0)
		{
			remove(key);
		}
	}
}
