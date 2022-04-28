package hu.qgears.commons;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Event listener that collects all received events into a {@link LinkedBlockingQueue}
 * thus it is possible to wait for events on an event processing thread.
 * @param <T>
 */
public class UtilEventQueue<T> implements UtilEventListener<T>, AutoCloseable {
	UtilEvent<T> ev;
	public final LinkedBlockingQueue<T> events=new LinkedBlockingQueue<>();
	public UtilEventQueue(UtilEvent<T> ev)
	{
		this.ev=ev;
		ev.addListener(this);
	}
	public UtilEventQueue(UtilListenableProperty<T> p)
	{
		this.ev=p.getPropertyChangedEvent();
		ev.addListener(this);
		events.add(p.getProperty());
	}
	@Override
	public void close() {
		ev.removeListener(this);
	}
	@Override
	public void eventHappened(T msg) {
		events.add(msg);
	}
}
