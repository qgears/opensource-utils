package hu.qgears.coolrmi.messages;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.remoter.CoolRMIProxy;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

/**
 * A list of remote calls that are sent to the server in a single transaction.
 */
public class CoolRMICallList extends AbstractCoolRMICall {
	private static final long serialVersionUID = 1L;
	private List<CoolRMICall> calls=new ArrayList<CoolRMICall>(); 
	public CoolRMICallList() {
		super();
	}

	public CoolRMICallList(long queryId) {
		super(queryId);
	}

	public void addMethodCall(CoolRMIProxy proxy, Method method, Object[] args, boolean stopOnException) {
		CoolRMICall call=new CoolRMICall(proxy.getRemoter().getNextCallId(), proxy.getId(), method.getName(), args, stopOnException);
		calls.add(call);
	}

	public void setIds(long callId) {
		queryId=callId;
	}

	@Override
	public void executeServerSide(final GenericCoolRMIRemoter coolRMIRemoter) throws IOException {
		coolRMIRemoter.execute(new Runnable() {
			@Override
			public void run() {
				boolean error=false;
				try {
					CoolRMIReplyList ret=new CoolRMIReplyList(getQueryId());
					for(CoolRMICall call: calls)
					{
						CoolRMIReply reply;
						if(error)
						{
							reply=new CoolRMIReply(call.getQueryId(), null, new CoolRMIException("Previous call in call list failed."));
						}else
						{
							reply=call.executeOnExecutorThread(coolRMIRemoter);
							if(call.isStopOnException() && reply.getException()!=null)
							{
								error=true;
							}
						}
						ret.addReply(reply);
					}
					coolRMIRemoter.send(ret);
				} catch (IOException e) {
					// We can not do anything clever here.
					e.printStackTrace();
				}
			}
		});
	}
	public boolean isEmpty()
	{
		return calls.isEmpty();
	}
}
