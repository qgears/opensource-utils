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

	public CoolRMIProxyPlaceHolder() {
		super();
	}

	public CoolRMIProxyPlaceHolder(long proxyId) {
		super();
		this.proxyId = proxyId;
	}

	public long getProxyId() {
		return proxyId;
	} 
}
