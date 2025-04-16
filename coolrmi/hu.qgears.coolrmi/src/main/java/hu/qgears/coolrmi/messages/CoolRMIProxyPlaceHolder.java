package hu.qgears.coolrmi.messages;

import java.io.Serializable;

/**
 * A placholder for proxy objects on communication line.
 * 
 * Client side: Proxy objects are represented by Java proxy that connects 
 * all calls to Cool RMI remoter.
 * 
 * Server side: Proxy objects are represented by server side object.
 * 
 * Communication line: Proxy objects are represented by this class (id is passed)
 * 
 * @author rizsi
 *
 */
public class CoolRMIProxyPlaceHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	private long proxyId;
	private String ifaceName;

	public CoolRMIProxyPlaceHolder() {
		super();
	}

	public CoolRMIProxyPlaceHolder(long proxyId, String ifaceName) {
		super();
		this.proxyId = proxyId;
		this.ifaceName=ifaceName;
	}

	public long getProxyId() {
		return proxyId;
	}
	/**
	 * Non-null value means that a new proxy object must be created on the client side.
	 * @return
	 */
	public String getIfaceName() {
		return ifaceName;
	}
}
