package hu.qgears.parser.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Cache an elem buffer instance for each thread.
 */
public class ElemBufferCache {
	private volatile ThreadLocal<ElemBuffer> tl=new ThreadLocal<>();
	private ExecutorService service;
	/**
	 * Get the buffer for the current thread.
	 * @return
	 */
	public ElemBuffer get()
	{
		ElemBuffer ret=tl.get();
		if(ret==null)
		{
			ret=new ElemBuffer();
			tl.set(ret);
		}
		return ret;
	}
	public Future<Void> enqueueTask(Callable<Void> task)
	{
		if(service==null)
		{
			throw new IllegalStateException("startExecutors have to be called first!");
		}
		return service.submit(task);
	}
	public void waitFinish() throws InterruptedException
	{
		service.shutdown();
		service.awaitTermination(60, TimeUnit.SECONDS);
		service=null;
		// Clear cached buffers
		tl=new ThreadLocal<>();
	}
	public ElemBufferCache startExecutors(int nThreads) {
		if(service!=null)
		{
			service.shutdown();
			service=null;
		}
		service=Executors.newFixedThreadPool(nThreads);
		return this;
	}
	/**
	 * For benchmark execute everything on the same thread.
	 * @return
	 */
	public ElemBufferCache startSameThreadExecutor() {
		if(service!=null)
		{
			service.shutdown();
			service=null;
		}
		service=new SameThreadService();
		return this;
	}
}
