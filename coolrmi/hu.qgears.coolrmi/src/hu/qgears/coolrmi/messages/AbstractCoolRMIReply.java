package hu.qgears.coolrmi.messages;

import java.io.Serializable;

abstract public class AbstractCoolRMIReply extends AbstractCoolRMIMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public AbstractCoolRMIReply()
	{
		
	}
	public AbstractCoolRMIReply(long queryId) {
		super(queryId);
	}
}
