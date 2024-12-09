package hu.qgears.commons.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Future that waits multiple other futures and signals ready when all children futures are ready.
 * A single child fails fails the whole future.
 * @param <T>
 */
public class MultiSignalFutureWrapper<T> extends SignalFutureWrapper<List<T>> {
	AtomicInteger remaining;
	List<SignalFutureWrapper<T>> ws;
	List<T> ret;
	boolean started=false;
	public MultiSignalFutureWrapper(List<SignalFutureWrapper<T>> ws)
	{
		this.ws=ws;
	}
	public MultiSignalFutureWrapper()
	{
		this.ws=new ArrayList<>();
	}
	public MultiSignalFutureWrapper<T> start()
	{
		synchronized (ws) {
			if(started)
			{
				throw new IllegalStateException("Already started");
			}
			started=true;
		}
		remaining=new AtomicInteger(ws.size());
		ret=new ArrayList<>();
		for(SignalFutureWrapper<T> s: ws)
		{
			ret.add(s.getSimple());
		}
		int i=0;
		for(SignalFutureWrapper<T> q: ws)
		{
			int index=i;
			q.addOnReadyHandler(t->{
				T value=q.getSimple();
				if(value!=null)
				{
					ret.set(index, value);
					int r=remaining.addAndGet(-1);
					if(r==0)
					{
						MultiSignalFutureWrapper.this.ready(ret, null);
					}
				}else
				{
					MultiSignalFutureWrapper.this.ready(null, q.getThrowable());
				}
			});
		}
		return this;
	}
	public void addFuture(SignalFutureWrapper<T> fut) {
		synchronized (ws) {
			if(started)
			{
				throw new IllegalStateException("Already started");
			}
			ws.add(fut);
		}
	}
}
