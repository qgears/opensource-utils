package hu.qgears.coolrmi;

import hu.qgears.coolrmi.remoter.CoolRMIServerSideObject;

/**
 * Server side proxy object that can be passed as through coolRMI.
 * callbackable object 
 * @author rizsi
 *
 */
public interface ICoolRMIServerSideProxy {
	CoolRMIServerSideObject getCoolRMIServerSideProxyObject();
}
