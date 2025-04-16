package hu.qgears.coolrmi.remoter;

import hu.qgears.coolrmi.ICoolRMIDisposable;
import hu.qgears.coolrmi.UtilEvent;

/**
 * Server side endpoint of a service proxy.
 * 
 * Access can be passed to client side by calling its
 * getClientSideHandle() method. 
 * @author rizsi
 *
 */
public class CoolRMIServerSideObject {
	private Object service;
	private boolean disposed=false;
	public boolean isDisposed() {
		return disposed;
	}
	private UtilEvent<Object> disposedEvent=new UtilEvent<Object>();
	public UtilEvent<Object> getDisposedEvent() {
		return disposedEvent;
	}
	public CoolRMIServerSideObject(long id, Class<?> iface, Object service) {
		super();
		this.id = id;
		this.iface = iface;
		this.service = service;
	}
	private Class<?> iface;
	public Class<?> getIface() {
		return iface;
	}
	public long getProxyId() {
		return id;
	}
	private long id;
	public Object getService() {
		return service;
	}
	public void dispose(GenericCoolRMIRemoter genericCoolRMIRemoter) {
		if(service instanceof ICoolRMIDisposable)
		{
			((ICoolRMIDisposable) service).disposeWhenDisconnected();
		}
		disposed=true;
		getDisposedEvent().eventHappened(null);
	}
}
