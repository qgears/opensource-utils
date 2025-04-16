package hu.qgears.coolrmi;

/**
 * Wrapper class that stores all information of an object
 * and its interface that can be shared.
 * @author rizsi
 *
 */
public class CoolRMIShareableObject {
	private Class<?> interface_;
	private Object service;
	/**
	 * Create service declaration.
	 * @param interface_ the interface class of the object (service)
	 * 	that is proxied to the other endpoint
	 * @param service the service implementation class
	 */
	public CoolRMIShareableObject(
			Class<?> interface_, Object service) {
		super();
		// Check whether service really implements interface
		interface_.cast(service);
		this.service = service;
		this.interface_=interface_;
	}
	/**
	 * the service implementation class that implements
	 * the interface 
	 * @return
	 */
	public Object getService() {
		return service;
	}
	/**
	 * The interface that can be remotely accessed
	 * on this service.
	 * @return
	 */
	public Class<?> getInterface() {
		return interface_;
	}
}
