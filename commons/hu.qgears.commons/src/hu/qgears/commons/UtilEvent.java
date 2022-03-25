package hu.qgears.commons;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Class that's instances represent an event and manages the listeners and
 * firing of them.
 * Features:
 *  - multithread access to listeners is allowed
 *  - listeners and events can be chained. 
 */
public class UtilEvent<T> implements UtilEventListener<T> {
	private static final Logger LOG = Logger.getLogger(UtilEvent.class);
	private static UtilEventListener<?>[] emptyArray=new UtilEventListener<?>[]{};
	
	private List<UtilEventListener<T>> listeners=null;
	private UtilEventListener<T>[] listenersArray;
	/**
	 * Add a listener to this event.
	 * @param l listener to be added
	 */
	public void addListener(UtilEventListener<T> l)
	{
		synchronized (this) {
			if(listeners==null)
			{
				listeners=new ArrayList<>();
			}
			listeners.add(l);
			listenersArray=null;
		}
	}
	/**
	 * Remove a listener from this event.
	 * @param l listener to be removed
	 */
	public void removeListener(UtilEventListener<T> l)
	{
		synchronized (this) {
			if(listeners!=null)
			{
				listeners.remove(l);
				if(listeners.isEmpty())
				{
					listeners=null;
				}
			}
			listenersArray=null;
		}
	}
	/**
	 * In case any listener fails the exception is just logged to System out. 
	 */
	@Override
	public void eventHappened(T msg)
	{
		UtilEventListener<T>[] ls;
		ls=getListenersArray();
		for(UtilEventListener<T> l:ls)
		{
			try
			{
				l.eventHappened(msg);
			}catch(Throwable t)
			{
				LOG.error("eventHappened",t);
			}
		}
	}
	@SuppressWarnings("unchecked")
	private UtilEventListener<T>[] getListenersArray() {
		synchronized (this) {
			if(listenersArray==null)
			{
				if(listeners==null)
				{
					listenersArray=(UtilEventListener<T>[])emptyArray;
				}else
				{
					listenersArray=listeners.toArray((UtilEventListener<T>[])emptyArray);
				}
			}
			return listenersArray;
		}
	}
	/**
	 * Get the current count of listeners added to this event.
	 * @return the current number of listeners added to this event.
	 */
	public int getNListeners()
	{
		synchronized (this) {
			if(listeners==null)
			{
				return 0;
			}
			return listeners.size();
		}
	}
}
