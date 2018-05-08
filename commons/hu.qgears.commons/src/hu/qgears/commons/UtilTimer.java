package hu.qgears.commons;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * Singleton timer service to execute tasks after a timeout.
 * 
 * The timeout is called on "an other" thread
 *  * The task must execute fast. In case of a long running process a worker thread must be used.
 *
 * Current implementation starts a new thread for each timer.
 * 
 * @author rizsi
 *
 */
public class UtilTimer {
	
	private static final Logger LOG = Logger.getLogger(UtilTimer.class);
	
	private static final UtilTimer INSTANCE = new UtilTimer();
	
	private UtilTimer() {
		// disable constructor of singleton class
	}

	public static UtilTimer getInstance() {
		return INSTANCE;
	}
	
	
	/**
	 * Execute callable when timeout expires.
	 * @param <T>
	 * @param timeoutMillis
	 * @param callable this is called when the timeout is triggered.
	 * @return {@link SignalFutureWrapper} object that can be used to add listeners to the timeout callable return value.
	 */
	public <T> SignalFutureWrapper<T> executeTimeout(final long timeoutMillis, final Callable<T> callable)
	{
		final SignalFutureWrapper<T> ret=new SignalFutureWrapper<T>();
		new Thread("Execute timeout"){
			public void run() {
				try {
					Thread.sleep(timeoutMillis);
					Object o=callable.call();
					ret.ready(o, null);
				} catch (Throwable e) {
					LOG.error("Interrupt",e);
					ret.ready(null, e);
				}
			};
		}.start();
		return ret;
	}
}
