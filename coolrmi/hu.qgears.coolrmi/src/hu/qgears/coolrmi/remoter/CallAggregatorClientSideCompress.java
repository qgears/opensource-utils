package hu.qgears.coolrmi.remoter;

import java.lang.reflect.Method;

import hu.qgears.coolrmi.messages.AbstractCoolRMICall;
import hu.qgears.coolrmi.messages.CoolRMICallList;

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
		currentList.addMethodCall(owner, method, args);
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
