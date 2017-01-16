package hu.qgears.coolrmi.remoter;

import java.lang.reflect.Method;

import hu.qgears.coolrmi.messages.AbstractCoolRMICall;
import hu.qgears.coolrmi.messages.CoolRMICallList;

/**
 * Call aggregator that aggregates all method calls with void return into a single call list.
 * The result of such compression is that remoting itself takes much less transactions and thus much less time
 * to execute.
 * 
 * The semantics of method execution changes a little:
 * 
 *  * void (return) methods are not executed until a flush call or until a non-void method call
 *  * In case a void method call throws an exception then it is not thrown by the call itself but is sent back to this object as a callback.
 *  * Execution of a call list can stop at exception or can go on executing the remaining calls (depending on global setting on this class).
 */
public class CallAggregatorClientSideCompress extends CallAggregatorClientSide
{
	private CoolRMICallList currentList=new CoolRMICallList();
	public CallAggregatorClientSideCompress(CoolRMIProxy owner) {
		super(owner);
	}

	private boolean isQueableMethod(Method m)
	{
		return m.getReturnType().isPrimitive()&&"void".equals(m.getReturnType().getName());
	}
	public AbstractCoolRMICall createCall(Method method, Object[] args) {
		boolean stopOnException=isStopOnException(method, args);
		currentList.addMethodCall(owner, method, args, stopOnException);
		if(!isQueableMethod(method))
		{
			long callId=owner.getRemoter().getNextCallId();
			currentList.setIds(callId);
			CoolRMICallList ret=currentList;
			currentList=new CoolRMICallList();
			return ret;
		}
		return null;
	}
	/**
	 * Should the execution of a call list be stopped in case of exception?
	 * Can be overridden in subclasses.
	 * @param method
	 * @param a
	 * @return
	 */
	protected boolean isStopOnException(Method method, Object a) {
		return false;
	}
	@Override
	public AbstractCoolRMICall flush() {
		if(!currentList.isEmpty())
		{
			CoolRMICallList ret=currentList;
			currentList=new CoolRMICallList();
			return ret;
		}
		return null;
	}
}
