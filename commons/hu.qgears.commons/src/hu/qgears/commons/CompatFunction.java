package hu.qgears.commons;

/**
 * Backward compatibility (JRE 1.7) replacement of java.util.function.Function.
 * 
 * Interface to execute a function that maps an object with type T to an other with type O.
 * 
 * The function must behave without side effects.
 *
 * @param <T>
 * @param <O>
 */
public interface CompatFunction<T,O> {
	/**
	 * Apply the function on the object t and return the value.
	 * The function must behave without side effects.
	 * @param t
	 * @return the result of the funtion applied to t.
	 */
	O apply(T t);
}
