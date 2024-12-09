package hu.qgears.commons;

/**
 * Event listener that knows its host and can be disposed.
 * Dispose will remove this listener from the host.
 * 
 * @author rizsi
 *
 * @param <T>
 */
public abstract class AbstractEventListener<T> implements UtilEventListener<T>, IDisposeable {
	private UtilEvent<T> host;
	public AbstractEventListener(UtilEvent<T> host) {
		this.host=host;
		this.host.addListener(this);
	}
	public UtilEvent<T> getHost() {
		return host;
	}
	@Override
	public void dispose() {
		host.removeListener(this);
	}
}
