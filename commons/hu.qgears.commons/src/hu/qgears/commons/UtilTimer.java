package hu.qgears.commons;

import java.util.concurrent.Callable;

/**
 * Singleton timer service to execute tasks after a timeout.
 * 
 * The timeout is called on "an other" thread
 *  * The task must execute fast. In case of a long running process a worker thread must be used.
 * 
 * TODO naive implementation - rewrite with a single working thread.
 * 
 * @author rizsi
 *
 */
public class UtilTimer {
	static UtilTimer instance=new UtilTimer();
	public static UtilTimer getInstance()
	{
		return instance;
	}
	/**
	 * Execute callable when timeout expires.
	 * @param <T>
	 * @param timeoutMillis
	 * @param callable
	 * @return
	 */
	public <T> void executeTimeout(final long timeoutMillis, final Callable<T> callable)
	{
		new Thread("Execute timeout"){
			public void run() {
				try {
					Thread.sleep(timeoutMillis);
					callable.call();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}
}
