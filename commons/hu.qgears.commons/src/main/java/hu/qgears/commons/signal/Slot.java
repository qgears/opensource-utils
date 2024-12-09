package hu.qgears.commons.signal;

/**
 * A slot can listen to a signal.
 * @author rizsi
 *
 * @param <T>
 */
public interface Slot<T> {
	/**
	 * This method is called when the signal event is fired
	 * @param value
	 */
	void signal(T value);
}
