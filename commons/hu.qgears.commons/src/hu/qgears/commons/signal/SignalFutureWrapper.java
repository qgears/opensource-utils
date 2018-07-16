package hu.qgears.commons.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.INamed;

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
public class SignalFutureWrapper<T> implements SignalFuture<T>, Callable<T>, INamed
{
	private Callable<T> callable;
	private Throwable exc;
	private boolean done;
	private boolean cancelled;
	private List<Slot<SignalFuture<T>>> listeners=null;
	private T ret;
	private String name="";

	/**
	 * Call this method when you want to finish
	 * this future - fire listeners and unblock get() calls.
	 * @param ret
	 * @param exc
	 */
	public void ready(T ret, Throwable exc) {
		ready(ret, exc, false);
	}

	private boolean ready(T ret, Throwable exc, boolean cancel) {
		boolean finishedNow=false;
		List<Slot<SignalFuture<T>>> ls;
		synchronized (this) {
			if(!done)
			{
				finishedNow=true;
				this.ret=(T)ret;
				this.exc=exc;
				done=true;
				cancelled=cancel;
				this.notifyAll();
			}
			ls=listeners;
			listeners=null;
		}
		if(finishedNow&&ls!=null)
		{
			for(Slot<SignalFuture<T>> listener:ls)
			{
				listener.signal(this);
			}
		}
		return finishedNow;
	}

	public SignalFutureWrapper(Callable<T> callable, String name) {
		super();
		this.callable = callable;
		this.name=name;
	}

	public SignalFutureWrapper(String name) {
		super();
		this.name=name;
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
		return ready(null, new CancellationException(), true);
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
				if(cancelled && exc instanceof CancellationException)
				{
					throw (CancellationException)exc;
				}
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
				unit.timedWait(this, timeout);
				synchronized (this) {
					if(!this.done)
					{
						throw new TimeoutException("Timeout: "+timeout+" "+unit+" '"+name+"'");
					}
				}
			}
			return get();	
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (this) {
			return cancelled;
		}
	}
	@Override
	public boolean isDone() {
		synchronized (this) {
			return done;
		}
	}
	@Override
	public void addOnReadyHandler(Slot<SignalFuture<T>> listener) {
		synchronized (this) {
			if(!this.done)
			{
				if(listeners==null)
				{
					listeners=new ArrayList<Slot<SignalFuture<T>>>();
				}
				listeners.add(listener);
				return;
			}
		}
		listener.signal(this);
	}
	@Override
	public T call() {
		if(!isCancelled())
		{
			try {
				T ret=callable.call();
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
		synchronized (this) {
			return exc!=null;
		}
	}

	@Override
	public Throwable getThrowable() {
		synchronized (this) {
			return exc;
		}
	}

	@Override
	public T getSimple() {
		synchronized (this) {
			return ret;
		}
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
