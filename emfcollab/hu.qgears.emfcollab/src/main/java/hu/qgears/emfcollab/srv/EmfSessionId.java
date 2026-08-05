package hu.qgears.emfcollab.srv;

/**
 * Session id to identify emf server client.
 * 
 * This id is public.
 * 
 * The username is to be filled by the server.
 * 
 * @author rizsi
 *
 */
public class EmfSessionId implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private long id;
	private String userName;
	public EmfSessionId(long id) {
		super();
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public boolean sameClient(EmfSessionId clientId) {
		return id==clientId.getId();
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
