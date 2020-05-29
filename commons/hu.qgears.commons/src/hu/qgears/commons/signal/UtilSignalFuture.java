package hu.qgears.commons.signal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Execute a Callable using an executor that returns a SignalFuture
 */
public class UtilSignalFuture {
	
	private UtilSignalFuture() {
		// disable constructor of utility class
	}

	/**
	 * Execute a Callable using an executor and return a SignalFuture.
	 * <p>
	 * See SignalFuture interface!
	 * 
	 * @param <T>
	 * @param executor
	 * @param callable
	 * @return
	 */
	public static <T> SignalFuture<T> submit(ExecutorService executor, Callable<T> callable)
	{
		SignalFutureWrapper<T> ret=new SignalFutureWrapper<T>(callable);
		/*
		 * the return value is accessed via SignalFutureWrapper, the future
		 * object created by submit is not relevant
		 */
		executor.submit(ret);//NOSONAR
		return ret;
	}
}
