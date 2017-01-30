package hu.qgears.coolrmi.messages;

import java.io.IOException;

import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

/**
 * Special reply object that means that this reply must not be sent now and an other
 * reply will be generated on an asynchronous thread.
 * 
 * It is useful on single thread servers.
 * @author rizsi
 *
 */
public class CoolRMIReplyAsync extends CoolRMIReply
{
	private GenericCoolRMIRemoter remoter;
	public CoolRMIReplyAsync(GenericCoolRMIRemoter remoter, long queryId) {
		super(queryId, null, null);
		this.remoter=remoter;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isAsync() {
		return true;
	}

	public void reply(Object ret, Throwable t) {
		CoolRMIReply reply=new CoolRMIReply(getQueryId(), ret, t);
		try {
			remoter.send(reply);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
