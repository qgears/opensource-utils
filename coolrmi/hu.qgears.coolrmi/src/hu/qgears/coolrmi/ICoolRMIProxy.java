package hu.qgears.coolrmi;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

/**
 * All cool RMI proxies implement this interface.
 * When the proxy is not used any longer it must be disposed.
 * @author rizsi
 *
 */
public interface ICoolRMIProxy {
	/**
	 * Dispose this proxy object. It can not be used any longer.
	 */
	void disposeProxy();
	
	/**
	 * Query whether the proxy object is disposed or not.
	 * @return
	 */
	boolean isProxyDisposed();
	
	/**
	 * Get the remoter that hosts this proxy object.
	 * @return
	 */
	CoolRMIRemoter getProxyHome();
}
