package hu.qgears.coolrmi.messages;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.CoolRMITimeoutException;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

public class CoolRMIFutureReply {
	private SignalFutureWrapper<AbstractCoolRMIReply> fut=new SignalFutureWrapper<AbstractCoolRMIReply>();
	private CoolRMIRemoter remoter;
	private long callId;
	public CoolRMIFutureReply(CoolRMIRemoter remoter, long callId)
	{
		this.remoter=remoter;
		this.callId=callId;
	}
	public void received(AbstractCoolRMIReply reply) {
		fut.ready(reply, null);
	}

	public AbstractCoolRMIReply waitReply() {
		try {
			return fut.get(remoter.getTimeoutMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e1) {
			throw new CoolRMITimeoutException(e1);
		} catch(ExecutionException e)
		{
			// Can not happen as we never set an exception on the future object
			throw new CoolRMIException(e);
		} catch (InterruptedException e) {
			throw new CoolRMIException(e);
		}
		finally
		{
			remoter.removeAwaitingReply(this);
		}
	}
	public long getCallId() {
		return callId;
	}

}
