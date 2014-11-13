package hu.qgears.emfcollab.srv;

/**
 * Session key object.
 * Server maps the session key to logged in user.
 * 
 * The value of the session key is secret shared between client and server.
 * @author rizsi
 *
 */
public class EmfSessionKey implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private String sessionKey;
	private EmfSessionId clientId; 

	public EmfSessionKey(String sessionKey, EmfSessionId clientId) {
		super();
		this.sessionKey = sessionKey;
		this.clientId=clientId;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public EmfSessionId getClientId() {
		return clientId;
	}
}
