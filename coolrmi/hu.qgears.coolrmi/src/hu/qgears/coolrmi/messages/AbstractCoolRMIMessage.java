package hu.qgears.coolrmi.messages;

import java.io.Serializable;

abstract public class AbstractCoolRMIMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long queryId;
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
	abstract public String getName();
	/**
	 * Callback when the last piece of this message has been sent through the (TCP) channel.
	 * Default implementation does nothing.
	 */
	public void sent() {
	}
}
