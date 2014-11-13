package hu.qgears.commons.signal;

import java.util.concurrent.Future;

/**
 * A future object that executes a signal when the
 * future is done.
 * 
 * @author rizsi
 *
 * @param <T> type of return of future
 */
public interface SignalFuture<T> extends Future<T> {
	/**
	 * The handler will be called when this 
	 * future object is done.
	 * Can be called back:
	 *  * on future's executor thread when the future is finished.
	 *  * on current thread if the future is already finished
	 * @param listener
	 */
	void addOnReadyHandler(Slot<SignalFuture<T>> listener);
	/**
	 * True when done and get() will throw exception.
	 * @return
	 */
	boolean isFailed();
	/**
	 * When isFailed is true returns the exception that happened in the callable.
	 * @return
	 */
	Throwable getThrowable();
	/**
	 * When isDone returns the returned object or null in case of errors.
	 * Never throws exception. Simple because it need not be surrounded with try/catch.
	 * But the caller must check returned value whether it is valid or not.
	 * @return
	 */
	T getSimple();
}
