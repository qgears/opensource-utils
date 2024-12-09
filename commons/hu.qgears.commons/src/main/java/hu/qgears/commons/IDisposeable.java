package hu.qgears.commons;

/**
 * Marker interface that marks objects that must be disposed
 * to release resources. (eg. native resource, reference as listener etc...)
 * Extends the standard {@link AutoCloseable} interface so that it is possible to use in constructs that 
 * require that interface.
 */
public interface IDisposeable extends NoExceptionAutoClosable {
	/**
	 * Free resources allocated by this object.
	 */
	void dispose();
	/**
	 * Check whether this object is disposed or not.
	 * @return
	 */
	boolean isDisposed();
	/**
	 * {@link AutoCloseable} interface - routed to dispose this object.
	 */
	@Override
	default void close() {
		dispose();
	};
}
