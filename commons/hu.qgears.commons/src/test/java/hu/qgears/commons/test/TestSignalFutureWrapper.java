package hu.qgears.commons.test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.commons.signal.Slot;

public class TestSignalFutureWrapper {
	class Listener implements Slot<SignalFuture<Object>>
	{
		private int n;
		private Object value;
		private Thread accessThread;
		@Override
		public void signal(SignalFuture<Object> value) {
			synchronized (this) {
				n++;
				if(n>1)
				{
					throw new RuntimeException("Listener called more than once.");
				}
				this.value=value.getSimple();
				accessThread=Thread.currentThread();
			}
		}
		public int getN() {
			synchronized (this) {
				return n;
			}
		}
		public Object getValue() {
			synchronized (this) {
				return value;
			}
		}
		public Thread getAccessThread() {
			synchronized (this) {
				return accessThread;
			}
		}
	}
	
	@BeforeClass
	public static void warmup() {
		//Ensure all classes are loaded that are used in timeout tests.
		//This fixes sporadic failure on the first executed test case in this class   
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		ret.addOnReadyHandler(o->{});
		try
		{
			ret.get(15, TimeUnit.MILLISECONDS);
		}catch(Exception e)
		{
			// Timeout exception is required
		}
	}
	
	@Test
	public void testTimeoutMillis() throws InterruptedException, ExecutionException, TimeoutException
	{
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.addOnReadyHandler(l);
		long t0=System.nanoTime();
		try
		{
			ret.get(15, TimeUnit.MILLISECONDS);
		}catch(TimeoutException e)
		{
			// Timeout exception is required
		}
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)>14);
		Assert.assertTrue(((t-t0)/1000000)<20);
		Assert.assertEquals(0, l.getN());
	}
	@Test
	public void testTimeoutMicros() throws InterruptedException, ExecutionException, TimeoutException
	{
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.addOnReadyHandler(l);
		long t0=System.nanoTime();
		try
		{
			ret.get(15000, TimeUnit.MICROSECONDS);
		}catch(TimeoutException e)
		{
			// Timeout exception is required
		}
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)>14);
		Assert.assertTrue(((t-t0)/1000000)<20);
		Assert.assertEquals(0, l.getN());
	}
	@Test
	public void test02() throws InterruptedException, ExecutionException, TimeoutException
	{
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.addOnReadyHandler(l);
		ret.cancel(false);
		long t0=System.nanoTime();
		try
		{
			ret.get(15, TimeUnit.MILLISECONDS);
		}catch(CancellationException e)
		{
			// Cancellation exception is required
		}
		Assert.assertTrue(ret.isDone());
		Assert.assertTrue(ret.isCancelled());
		Assert.assertTrue(ret.isFailed());
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)<14);
		Assert.assertEquals(1, l.getN());
	}
	@Test
	public void test03() throws InterruptedException, ExecutionException, TimeoutException
	{
		Object value="value";
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.addOnReadyHandler(l);
		ret.ready(value, null);
		long t0=System.nanoTime();
		Object o=ret.get(15, TimeUnit.MILLISECONDS);
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)<14);
		Assert.assertEquals(1, l.getN());
		Assert.assertEquals(value, o);
		Assert.assertEquals(value, l.getValue());
	}
	@Test
	public void test04() throws InterruptedException, ExecutionException, TimeoutException
	{
		Object value="value";
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.ready(value, null);
		ret.addOnReadyHandler(l);
		long t0=System.nanoTime();
		Object o=ret.get(15, TimeUnit.MILLISECONDS);
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)<14);
		Assert.assertEquals(1, l.getN());
		Assert.assertEquals(value, o);
		Assert.assertEquals(value, l.getValue());
	}
	@Test
	public void testException() throws InterruptedException, ExecutionException, TimeoutException
	{
		Throwable exc=new RuntimeException();
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.ready(null, exc);
		ret.addOnReadyHandler(l);
		long t0=System.nanoTime();
		try {
			ret.get(15, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			Assert.assertEquals(exc,  e.getCause());
		}
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)<14);
		Assert.assertEquals(1, l.getN());
	}
	@Test
	public void testExceptionCancellatoon() throws InterruptedException, ExecutionException, TimeoutException
	{
		Throwable exc=new CancellationException();
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		ret.ready(null, exc);
		ret.addOnReadyHandler(l);
		long t0=System.nanoTime();
		try {
			ret.get(15, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			Assert.assertEquals(exc,  e.getCause());
		}
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)<14);
		Assert.assertEquals(1, l.getN());
		Assert.assertTrue(ret.isDone());
		Assert.assertFalse(ret.isCancelled());
		Assert.assertTrue(ret.isFailed());
	}
	@Test
	public void test05() throws InterruptedException, ExecutionException, TimeoutException
	{
		final LinkedBlockingQueue<Object> queue=new LinkedBlockingQueue<Object>();
		final Object value="value";
		final SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		Thread th=new Thread(){
			public void run() {
				try {
					// Make sure that we only raise ready after the listener is added.
					queue.take();
					ret.ready(value, null);
					// After all listeners were fired this is called.
					queue.put(new Object());
				} catch (InterruptedException e) {
					// Does not happen
					e.printStackTrace();
				}
			};
		};
		th.start();
		ret.addOnReadyHandler(l);
		// After adding the listener the other thread can go on.
		queue.put(new Object());
		Object o=ret.get();
		// Wait until all listeners are finished on the other thread 
		// (without this it is possible that n is not incremented yet when it is asserted to be 1)
		queue.take();
		Assert.assertEquals(1, l.getN());
		Assert.assertEquals(value, o);
		Assert.assertEquals(value, l.getValue());
		Assert.assertEquals(th, l.getAccessThread());
	}
	@Test
	public void test06() throws InterruptedException, ExecutionException, TimeoutException
	{
		final Object value="value";
		final SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		Listener l=new Listener();
		long t0=System.nanoTime();
		new Thread(){
			public void run() {
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ret.ready(value, null);
			};
		}.start();
		Assert.assertFalse(ret.isDone());
		Assert.assertFalse(ret.isCancelled());
		Assert.assertFalse(ret.isFailed());
		Object o=ret.get();
		ret.addOnReadyHandler(l);
		long t=System.nanoTime();
		Assert.assertTrue(((t-t0)/1000000)>14);
		Assert.assertFalse(ret.isFailed());
		Assert.assertFalse(ret.isCancelled());
		Assert.assertTrue(ret.isDone());
		Assert.assertEquals(1, l.getN());
		Assert.assertEquals(value, o);
		Assert.assertEquals(value, l.getValue());
		Assert.assertEquals(Thread.currentThread(), l.getAccessThread());
	}
}
