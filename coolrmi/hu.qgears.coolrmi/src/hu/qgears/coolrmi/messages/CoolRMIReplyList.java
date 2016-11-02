package hu.qgears.coolrmi.messages;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.coolrmi.remoter.CallAggregatorClientSide;
import hu.qgears.coolrmi.remoter.CoolRMIProxy;

public class CoolRMIReplyList extends AbstractCoolRMIMethodCallReply
{
	private static final long serialVersionUID = 1L;
	private List<CoolRMIReply> replies=new ArrayList<CoolRMIReply>();

	public void addReply(CoolRMIReply reply) {
		replies.add(reply);
	}

	@Override
	public Object evaluateOnClientSide(CoolRMIProxy coolRMIProxy, boolean returnLast) throws Throwable {
		// All but the last reply are processed using a callback for errors
		CallAggregatorClientSide aggregator=coolRMIProxy.getCallAggregator();
		for(int i=0;i<replies.size()-(returnLast?1:0);++i)
		{
			CoolRMIReply rep=replies.get(i);
			rep.resolveArgumentsOnClient(coolRMIProxy.getRemoter());
			aggregator.methodCallReplied(rep);
		}
		if(returnLast)
		{
			CoolRMIReply rep=replies.get(replies.size()-1);
			return rep.evaluateOnClientSide(coolRMIProxy, true);
		}
		return null;
	}

}
