package hu.qgears.coolrmi.messages;

import java.io.Serializable;


public class CoolRMICreateProxy
	extends AbstractCoolRMIMessage
	implements Serializable {
	private static final long serialVersionUID = 1L;
	private long proxyId;
	private String ifaceName;
	public String getIfaceName() {
		return ifaceName;
	}

	public long getProxyId() {
		return proxyId;
	}

	public CoolRMICreateProxy() {
		
	}

	public CoolRMICreateProxy(long queryId, 
			long proxyId,
			String ifaceName) {
		super(queryId);
		this.proxyId = proxyId;
		this.ifaceName=ifaceName;
	}

	@Override
	public String toString() {
		return "create proxy "+proxyId+" "+ifaceName;
	}
}
