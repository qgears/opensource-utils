package hu.qgears.commons.signal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Execute a Callable using an executor that returns a SignalFuture
 * @author rizsi
 *
 */
public class UtilSignalFuture {
	/**
	 * Execute a Callable using an executor and return a SignalFuture.
	 * 
	 * See SignalFuture inerface!
	 * 
	 * @param <T>
	 * @param executor
	 * @param callable
	 * @return
	 */
	public static <T> SignalFuture<T> submit(ExecutorService executor, Callable<T> callable)
	{
		SignalFutureWrapper<T> ret=new SignalFutureWrapper<T>(callable);
		executor.submit(ret);
		return ret;
	}
}
