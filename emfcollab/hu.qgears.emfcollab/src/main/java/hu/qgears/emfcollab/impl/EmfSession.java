package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.srv.EmfSessionId;

public class EmfSession {
	private String userName;
	private EmfSessionId sessionId;
	public EmfSessionId getSessionId() {
		return sessionId;
	}
	public EmfSession(String userName, EmfSessionId sessionId) {
		super();
		this.userName=userName;
		this.sessionId=sessionId;
	}
	public String getUserName() {
		return userName;
	}
}
