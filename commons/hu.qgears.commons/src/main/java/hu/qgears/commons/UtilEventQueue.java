package hu.qgears.commons;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Event listener that collects all received events into a {@link LinkedBlockingQueue}
 * thus it is possible to wait for events on an event processing thread.
 * @param <T>
 */
public class UtilEventQueue<T> implements UtilEventListener<T>, AutoCloseable {
	private UtilEvent<T> ev;
	public final LinkedBlockingQueue<T> events=new LinkedBlockingQueue<>();
	private NoExceptionAutoClosable close;
	public UtilEventQueue(UtilEvent<T> ev)
	{
		this.ev=ev;
		ev.addListener(this);
	}
	public UtilEventQueue(UtilListenableProperty<T> p)
	{
		close=p.addListenerWithInitialTrigger(this);
	}
	@Override
	public void close() {
		if(close!=null)
		{
			close.close();
			close=null;
		}
		if(ev!=null)
		{
			ev.removeListener(this);
			ev=null;
		}
	}
	@Override
	public void eventHappened(T msg) {
		events.add(msg);
	}
	/**
	 * Block execution of thread until the required value is set in the property.
	 * @param <T>
	 * @param prop
	 * @param reqValue
	 * @throws InterruptedException
	 */
	public static <T> void waitValue(UtilListenableProperty<T> prop, T reqValue) throws InterruptedException
	{
		try(UtilEventQueue<T> st=new UtilEventQueue<>(prop))
		{
			T value=st.events.take();
			while(value!=reqValue)
			{
				value=st.events.take();
			}
		}
	}
	public static <T> void waitValue(UtilListenableProperty<T> prop, T reqValue, long timeoutMillis) throws InterruptedException, TimeoutException
	{
		long t0=System.nanoTime();
		long remainingMillis=timeoutMillis;
		try(UtilEventQueue<T> st=new UtilEventQueue<>(prop))
		{
			T value=st.events.poll(remainingMillis, TimeUnit.MILLISECONDS);
			while(value!=reqValue)
			{
				remainingMillis=checkTimeout(t0, timeoutMillis);
				value=st.events.poll(remainingMillis, TimeUnit.MILLISECONDS);
			}
		}
	}
	private static long checkTimeout(long t0, long timeoutMillis) throws TimeoutException {
		long t=System.nanoTime();
		long diffMilli=(t-t0)/1000000l;
		long remaining=timeoutMillis-diffMilli;
		if(remaining<0)
		{
			throw new TimeoutException("Timeout expired: "+timeoutMillis+"millis");
		}
		return remaining;
	}
	/**
	 * Block execution of thread until the required value is set in the property.
	 * @param <T>
	 * @param prop
	 * @param condition Exit when condition is true. null can not fulfill the condition because null is treated as no value available by the poll API.
	 * @param timeout timeout value measured in unit negative value will cause {@link TimeoutException} at once
	 * @param unit timeout unit. null is valid and means no timeout.
	 * @return value that fulfilled the condition
	 * @throws InterruptedException
	 * @throws TimeoutException in case timeout happens before value that fulfills the condition arrives.
	 */
	public static <T> T waitCondition(UtilListenableProperty<T> prop, Function<T,Boolean> condition, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
	{
		long t0=System.nanoTime();
		long toNano;
		if(unit!=null)
		{
			toNano=TimeUnit.NANOSECONDS.convert(timeout, unit);
			if(toNano<0)
			{
				throw new TimeoutException();
			}
		}else
		{
			toNano=-1;
		}
		try(UtilEventQueue<T> st=new UtilEventQueue<>(prop))
		{
			while(true)
			{
				T value;
				if(toNano==-1)
				{
					value=st.events.take();
				}else
				{
					value=st.events.poll(toNano, TimeUnit.NANOSECONDS);
				}
				if(value!=null)
				{
					if(condition.apply(value))
					{
						return value;
					}
				}
				if(toNano!=-1)
				{
					long spent=System.nanoTime()-t0;
					toNano-=spent;
					if(toNano<1)
					{
						throw new TimeoutException();
					}
				}
			}
		}
	}
}
