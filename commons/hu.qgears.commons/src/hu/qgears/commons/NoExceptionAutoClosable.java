package hu.qgears.commons;

/**
 * Auto closeable that guarantees that it can be closed without throwing an exception.
 */
public interface NoExceptionAutoClosable extends AutoCloseable
{
	@Override
	default void close() {
	}
}
