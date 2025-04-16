package hu.qgears.coolrmi;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

/**
 * Marker interface that marks that an object have to be sent to the other side
 * through a proxy that allows callback to the object.
 * 
 * This is the same feature as a {@link CoolRMIServiceRegistry}.addProxyType
 * Can be used when the implementation knows about CoolRMI.
 */
public interface ICoolRMIAutoProxy {
	/**
	 * Get the interface of which methods of this object are proxied.
	 * @return
	 */
	Class<?> getProxyInterface();
}
