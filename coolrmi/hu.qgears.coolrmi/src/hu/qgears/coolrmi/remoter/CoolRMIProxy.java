package hu.qgears.coolrmi.remoter;

import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.ICoolRMIProxy;
import hu.qgears.coolrmi.messages.CoolRMICall;
import hu.qgears.coolrmi.messages.CoolRMIReply;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;



/**
 * Representation of a proxy endpoint on the CoolRMI
 * remoter.
 * Uses Java proxy mechanism to pass calls to implementation class through network.
 * @author rizsi
 *
 */
public class CoolRMIProxy implements InvocationHandler {
	long id;
	CoolRMIRemoter home;
	boolean disposed=false;
	private ICoolRMIProxy proxyObject;
	public ICoolRMIProxy getProxyObject() {
		return proxyObject;
	}
	CoolRMIProxy(CoolRMIRemoter home, long id, Class<?> interface_)
	{
		this.home=home;
		this.id=id;
		proxyObject=(ICoolRMIProxy)Proxy.newProxyInstance(
				home.getClassLoader(),
				new Class<?>[]{interface_,
				ICoolRMIProxy.class},
				this);
	}
	public boolean isDisposed() {
		return disposed;
	}
	public void dispose()
	{
		home.remove(this);
		disposed=true;
	}
	public long getId() {
		return id;
	}
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if(method.getName().equals("disposeProxy"))
		{
			this.dispose();
			return null;
		}else if(method.getName().equals("isProxyDisposed"))
		{
			return disposed;
		}else if(method.getName().equals("getProxyHome"))
		{
			return home;
		}else
		{
			if(disposed)
			{
				throw new CoolRMIException("Proxy is already disposed.");
			}
			CoolRMIReply reply=null;
			try {
				long callId=home.getNextCallId();
//				TODO replaceProxiesInArgs(args);
				args=home.resolveProxyInParamersServerSide(args);
				CoolRMICall call = new CoolRMICall(callId,
						id, method.getName(),
						args);
				home.sendCall(call);
				reply=(CoolRMIReply) home.getAbstractReply(call.getQueryId());
				if (reply.getException() == null) {
					Object ret=reply.getRet();
					ret=home.resolveProxyInParamerClientSide(ret);
					return ret;
				}
			} catch (Throwable t) {
				throw new CoolRMIException("Exception doing the RMI", t);
			}
			if (reply != null) {
				throw reply.getException();
			} else {
				throw new CoolRMIException("Internal error");
			}
		}
	}
}
