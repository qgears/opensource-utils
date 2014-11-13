package hu.qgears.commons;

/**
 * Marker interface that marks objects that must be disposed
 * to release resources. (eg. native resource, reference as listener etc...)
 * @author rizsi
 *
 */
public interface IDisposeable {
	/**
	 * Free resources allocated by this object.
	 */
	void dispose();
	/**
	 * Check whether this object is disposed or not.
	 * @return
	 */
	boolean isDisposed();
}
