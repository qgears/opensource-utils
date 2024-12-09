package hu.qgears.commons;

/**
 * DTO for a pair of obejcts.
 * equals and hashCode is overridden in a way that it can be used in hashmaps as complex key.
 * @author rizsi
 *
 * @param <T>
 * @param <U>
 */
public class Pair <T,U>
{
	private T a;
	private U b;
	private boolean hashInited;
	private int hashCode;
	public Pair(T a, U b) {
		super();
		this.a = a;
		this.b = b;
	}
	public T getA() {
		return a;
	}
	public U getB() {
		return b;
	}
	@Override
	public int hashCode() {
		if(!hashInited)
		{
			hashInited=true;
			hashCode=h(a)^h(b);
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
		if(obj instanceof Pair<?, ?>)
		{
			Pair<?, ?> other=(Pair<?, ?>) obj;
			return UtilEquals.safeEquals(other.getA(), a)&&
					UtilEquals.safeEquals(other.getB(), b);
		}
		return super.equals(obj);
	}
}
