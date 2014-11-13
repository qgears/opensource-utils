package hu.qgears.coolrmi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Class that's instances represent an event and manages the listeners and
 * firing of them.
 * Features:
 *  - multithread access to listeners is allowed
 *  - listeners and events can be chained. 
 * @author rizsi
 *
 */
public class UtilEvent<T> implements UtilEventListener<T> {
	private List<UtilEventListener<T>> listeners=new ArrayList<UtilEventListener<T>>();
	/**
	 * Add a listener to this event.
	 * @param l listener to be added
	 */
	public void addListener(UtilEventListener<T> l)
	{
		synchronized (listeners) {
			listeners.add(l);
		}
	}
	/**
	 * Remove a listener from this event.
	 * @param l listener to be removed
	 */
	public void removeListener(UtilEventListener<T> l)
	{
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	public void eventHappened(final T msg, Executor executor)
	{
		executor.execute(new Runnable(){
			@Override
			public void run() {
				eventHappened(msg);
			}
		});
	}
	/**
	 * In case any listener fails the exception is just logged to System out. 
	 */
	@Override
	public void eventHappened(T msg)
	{
		List<UtilEventListener<T>> ls;
		synchronized (listeners) {
			ls=new ArrayList<UtilEventListener<T>>(listeners);
		}
		for(UtilEventListener<T> l:ls)
		{
			try
			{
				l.eventHappened(msg);
			}catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
