package hu.qgears.coolrmi;

/**
 * Wrapper class that stores all information of a service that
 * can be installed on a server.
 * @author rizsi
 *
 */
public class CoolRMIService extends CoolRMIShareableObject {
	private String name;
	/**
	 * Create service declaration.
	 * @param name name of the service
	 * @param interface_ the interface class of the service that is proxied to the client
	 * @param service the service implementation class
	 */
	public CoolRMIService(
			String name,
			Class<?> interface_, Object service) {
		super(interface_, service);
		this.name=name;
	}
	public String getName() {
		return name;
	}
}
