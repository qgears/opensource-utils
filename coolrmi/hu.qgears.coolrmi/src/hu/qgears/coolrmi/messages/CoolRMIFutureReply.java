package hu.qgears.coolrmi.messages;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.commons.signal.Slot;
import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.CoolRMIReplyHandler;
import hu.qgears.coolrmi.CoolRMITimeoutException;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

public class CoolRMIFutureReply {
	private SignalFutureWrapper<AbstractCoolRMIReply> fut=new SignalFutureWrapper<AbstractCoolRMIReply>();
	private GenericCoolRMIRemoter remoter;
	private long callId;
	private CoolRMIReplyHandler handler;
	public CoolRMIFutureReply(GenericCoolRMIRemoter remoter, long callId)
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
			throw new CoolRMITimeoutException("Timeout executing call: "+callId, e1);
		} catch(ExecutionException e)
		{
			// Can not happen as we never set an exception on the future object
			throw new CoolRMIException("Internal exception on call: "+callId, e);
		} catch (InterruptedException e) {
			throw new CoolRMIException("Interrupted exception on call: "+callId, e);
		}
		finally
		{
			remoter.removeAwaitingReply(this);
		}
	}
	public long getCallId() {
		return callId;
	}
	public void cancelled() {
		fut.cancel(true);
	}
	public void registerListener(final CoolRMIReplyHandler handler) {
		fut.addOnReadyHandler(new Slot<SignalFuture<AbstractCoolRMIReply>>() {
			
			@Override
			public void signal(SignalFuture<AbstractCoolRMIReply> value) {
				if(value.getSimple() instanceof CoolRMIReply)
				{
					CoolRMIReply r=(CoolRMIReply) value.getSimple();
					handler.handle(r);
				}
			}
		});
	}
}
