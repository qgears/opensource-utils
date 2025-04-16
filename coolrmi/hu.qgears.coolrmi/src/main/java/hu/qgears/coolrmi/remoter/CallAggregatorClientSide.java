package hu.qgears.coolrmi.remoter;

import java.lang.reflect.Method;

import hu.qgears.coolrmi.messages.AbstractCoolRMICall;
import hu.qgears.coolrmi.messages.CoolRMICall;
import hu.qgears.coolrmi.messages.CoolRMIReply;

/**
 * Aggregate method calls to be sent in a single transaction.
 */
public class CallAggregatorClientSide {
	CoolRMIProxy owner;
	
	
	public CallAggregatorClientSide(CoolRMIProxy owner) {
		super();
		this.owner = owner;
	}

	public AbstractCoolRMICall createCall(Method method, Object[] args) {
		long callId=owner.getRemoter().getNextCallId();
		CoolRMICall call = new CoolRMICall(callId,
				owner.getId(), method.getName(),
				args, false);
		return call;
	}

	public AbstractCoolRMICall flush() {
		return null;
	}

	public void methodCallReplied(CoolRMIReply rep) {
		if(rep.getException()!=null)
		{
			handleException(rep, rep.getException());
		}
	}
	/**
	 * Handle exception (when method is executed in an asynchronous aggregated way).
	 * Default implementation logs to syserr. Subclasses may override. 
	 * @param rep
	 * @param exception
	 */
	protected void handleException(CoolRMIReply rep, Throwable exception) {
		exception.printStackTrace();
	}
}
