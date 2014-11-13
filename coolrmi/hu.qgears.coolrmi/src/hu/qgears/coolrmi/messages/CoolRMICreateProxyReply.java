package hu.qgears.coolrmi.messages;

import java.io.Serializable;


public class CoolRMICreateProxyReply
	extends AbstractCoolRMIReply
	implements Serializable {
	private static final long serialVersionUID = 1L;

	public CoolRMICreateProxyReply() {
		
	}

	public CoolRMICreateProxyReply(long queryId) {
		super(queryId);
	}

	@Override
	public String toString() {
		return "proxy created";
	}
}
