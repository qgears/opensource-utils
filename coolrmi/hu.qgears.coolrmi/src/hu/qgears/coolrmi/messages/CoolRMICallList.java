package hu.qgears.coolrmi.messages;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import hu.qgears.coolrmi.remoter.CoolRMIProxy;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

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

	public void addMethodCall(CoolRMIProxy proxy, Method method, Object[] args) {
		CoolRMICall call=new CoolRMICall(proxy.getRemoter().getNextCallId(), proxy.getId(), method.getName(), args);
		calls.add(call);
	}

	public void setIds(long callId) {
		queryId=callId;
	}

	@Override
	public void executeServerSide(final CoolRMIRemoter coolRMIRemoter, Executor serverSideExecutor) throws IOException {
		serverSideExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					CoolRMIReplyList ret=new CoolRMIReplyList();
					for(CoolRMICall call: calls)
					{
						CoolRMIReply reply=call.executeOnExecutorThread(coolRMIRemoter);
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
