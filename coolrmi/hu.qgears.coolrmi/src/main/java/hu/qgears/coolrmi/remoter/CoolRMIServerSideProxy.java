package hu.qgears.coolrmi.remoter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import hu.qgears.coolrmi.ICoolRMIServerSideProxy;



/**
 * Representation of a server side proxy object that can be passed to 
 * clients through Cool RMI remoter.
 * Uses Java proxy mechanism to create a class that implements the requested interface
 * but it is never used to call the server object.
 * @author rizsi
 *
 */
public class CoolRMIServerSideProxy implements InvocationHandler {
	private CoolRMIServerSideObject obj;
	private ICoolRMIServerSideProxy proxyObject;
//	private GenericCoolRMIRemoter home;
	public ICoolRMIServerSideProxy getProxyObject() {
		return proxyObject;
	}
	CoolRMIServerSideProxy(GenericCoolRMIRemoter home,
			CoolRMIServerSideObject obj)
	{
//		this.home=home;
		this.obj=obj;
		proxyObject=(ICoolRMIServerSideProxy)Proxy.newProxyInstance(
				home.getClassLoader(),
				new Class<?>[]{obj.getIface(),
			ICoolRMIServerSideProxy.class},
				this);
	}
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if(method.getName().equals("getCoolRMIServerSideProxyObject"))
		{
			return obj;
		}
		return null;
	}
}
