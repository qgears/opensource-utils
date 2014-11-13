package hu.qgears.commons;

/**
 * DTO for a pair of obejcts.
 * Does not override equals!
 * @author rizsi
 *
 * @param <T>
 * @param <U>
 */
public class Pair <T,U>
{
	private T a;
	private U b;
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
}
