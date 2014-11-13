package hu.qgears.coolrmi.messages;

import java.io.Serializable;

abstract public class AbstractCoolRMIMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private long queryId;
	public AbstractCoolRMIMessage()
	{
		
	}
	public AbstractCoolRMIMessage(long queryId) {
		super();
		this.queryId = queryId;
	}

	public long getQueryId() {
		return queryId;
	}
}
