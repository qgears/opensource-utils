package hu.qgears.coolrmi.remoter;

/**
 * Marker interface of parameter/return type objects that this specific object must be serialized in a
 * special way by replacing the original object with a serializable placeholder.
 * This is the same as the {@link CoolRMIReplaceEntry} feature but can only be used when the
 * remoting interface is designed with CoolRMI in mind and the interface can depend on this interface.
 */
public interface ICoolRMIReplaceType {
	/**
	 * Replace the object for serialization with a different serializable object.
	 * Should be an {@link IReplaceSerializable} so that it is replaced again on the receiver side.
	 * @return
	 */
	Object coolRMIReplaceObject();
}
