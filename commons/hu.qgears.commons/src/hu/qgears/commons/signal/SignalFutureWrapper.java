package hu.qgears.commons.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class implements the SignalFuture interface. Allows the user of this
 * class to execute a task when the signal is finished.
 * <p>
 * The class can also be used to block execution until an event happens (to use
 * Future.get without an executor but with an event)
 * 
 * @author rizsi
 *
 * @param <T>
 */
public class SignalFutureWrapper<T> implements SignalFuture<T>, Callable<T>
{
	private Callable<T> callable;
	private Throwable exc;
	private boolean done;
	private boolean cancelled;
	private List<Slot<SignalFuture<T>>> listeners=new ArrayList<Slot<SignalFuture<T>>>();
	private T ret;

	/**
	 * Call this method when you want to finish
	 * this future - fire listeners and unblock get() calls.
	 * @param ret
	 * @param exc
	 */
	@SuppressWarnings("unchecked")
	public void ready(Object ret, Throwable exc) {
		synchronized (this) {
			this.ret=(T)ret;
			this.exc=exc;
			done=true;
			this.notifyAll();
		}
		for(Slot<SignalFuture<T>> listener:listeners)
		{
			listener.signal(this);
		}
	}

	public SignalFutureWrapper(Callable<T> callable) {
		super();
		this.callable = callable;
	}

	public SignalFutureWrapper() {
		super();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		this.cancelled=true;
		return false;
	}
	@Override
	public T get() throws InterruptedException, ExecutionException {
		synchronized (this) {
			if(!this.done)
			{
				this.wait(); 
			}
			if(exc!=null)
			{
				throw new ExecutionException(exc);
			}
			return (T)ret;	
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		synchronized (this) {
			if(!this.done)
			{
				this.wait(unit.toMillis(timeout));
				if(!this.done)
				{
					throw new TimeoutException();
				}
			}
			return get();	
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public boolean isDone() {
		return done;
	}
	@Override
	public void addOnReadyHandler(Slot<SignalFuture<T>> listener) {
		synchronized (this) {
			if(!this.done)
			{
				listeners.add(listener);
				return;
			}
		}
		listener.signal(this);
	}
	@Override
	public T call() {
		if(!cancelled)
		{
			try {
				Object ret=callable.call();
				ready(ret, null);
			} catch (Exception e) {
				ready(null, e);
			}
		}else
		{
			ready(null, new RuntimeException("callable cancelled before execution"));
		}
		return ret;
	}

	@Override
	public boolean isFailed() {
		return exc!=null;
	}

	@Override
	public Throwable getThrowable() {
		return exc;
	}

	@Override
	public T getSimple() {
		/*
		 * It is not guaranteed, that the returned object is valid, so the
		 * synchronization is the responsibility of the caller. See the
		 * specification of the method.
		 */
		return ret;//NOSONAR
	}
}
