package hu.qgears.coolrmi.remoter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.ICoolRMIProxy;
import hu.qgears.coolrmi.messages.AbstractCoolRMICall;
import hu.qgears.coolrmi.messages.AbstractCoolRMIMethodCallReply;
import hu.qgears.coolrmi.messages.CoolRMIFutureReply;



/**
 * Representation of a proxy endpoint on the CoolRMI
 * remoter.
 * Uses Java proxy mechanism to pass calls to implementation class through network.
 * @author rizsi
 *
 */
public class CoolRMIProxy implements InvocationHandler {
	private long id;
	private GenericCoolRMIRemoter remoter;
	private boolean disposed=false;
	private ICoolRMIProxy proxyObject;
	private CallAggregatorClientSide callAggregator=new CallAggregatorClientSide(this);
	public ICoolRMIProxy getProxyObject() {
		return proxyObject;
	}
	public CoolRMIProxy(GenericCoolRMIRemoter remoter, long id, Class<?> interface_)
	{
		this.remoter=remoter;
		this.id=id;
		proxyObject=(ICoolRMIProxy)Proxy.newProxyInstance(
				remoter.getClassLoader(),
				new Class<?>[]{interface_,
				ICoolRMIProxy.class},
				this);
	}
	public boolean isDisposed() {
		return disposed;
	}
	public void dispose()
	{
		remoter.remove(this);
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
			return remoter;
		}else if(method.getName().equals("getProxyObject"))
		{
			return this;
		}else
		{
			if(disposed)
			{
				throw new CoolRMIException("Proxy is already disposed.");
			}
			AbstractCoolRMIMethodCallReply reply;
			try {
				args=remoter.resolveProxyInParamersServerSide(args);
				AbstractCoolRMICall call=callAggregator.createCall(method, args);
				if(call!=null)
				{
					CoolRMIFutureReply replyFuture=remoter.getAbstractReply(call.getQueryId());
					remoter.sendCall(call);
					reply=(AbstractCoolRMIMethodCallReply)replyFuture.waitReply();
					reply.evaluateOnClientSide(this, true);
				}else
				{
					return null;
				}
			} catch (Throwable t) {
				throw new CoolRMIException("Exception doing the RMI", t);
			}
			if (reply.getException() == null) {
				return reply.getRet();
			}else
			{
				throw reply.getException();
			}
		}
	}
	public GenericCoolRMIRemoter getRemoter() {
		return remoter;
	}
	public CallAggregatorClientSide getCallAggregator() {
		return callAggregator;
	}
	/**
	 * Set up the call aggregating mechanism. See {@link CallAggregatorClientSideCompress}
	 * @param callAggregator
	 */
	public void setCallAggregator(CallAggregatorClientSide callAggregator) {
		this.callAggregator = callAggregator;
	}
	/**
	 * Force aggregated method calls to be sent to the server
	 * in case all of them are void calls and thus aggregated. See {@link CallAggregatorClientSideCompress}
	 */
	public void flushAggregated()
	{
		AbstractCoolRMICall call=callAggregator.flush();
		if(call!=null)
		{
			try {
				CoolRMIFutureReply replyFut=remoter.getAbstractReply(call.getQueryId());
				remoter.sendCall(call);
				AbstractCoolRMIMethodCallReply reply=(AbstractCoolRMIMethodCallReply) replyFut.waitReply();
				reply.evaluateOnClientSide(this, false);
			} catch (Throwable e) {
				// Not possible in this case
				e.printStackTrace();
			}
		}
	}
}
