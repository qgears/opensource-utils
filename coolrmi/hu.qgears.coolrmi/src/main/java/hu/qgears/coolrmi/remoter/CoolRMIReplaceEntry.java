package hu.qgears.coolrmi.remoter;

/**
 * Object serialization replace entry.
 * 
 * Can be used in cases when an interface has to be used for serialization but some of the arguments
 * passed through the interface (argument or return value) are not serializable.
 * 
 * The easiest solution is to make those classes serializable. But in some cases it is not possible to change the API.
 * In those cases the replace entries can help.
 * 
 * For each not serializable class a replace entry must be registered. The doReplace method must be implemented to 
 * create a serializable representation of the original object. This representation is sent through the (TCP) channel.
 * On the other side the object is deserialized and then the {@link IReplaceSerializable}.readResolve() method is called to
 * re-create the equivalent of the original object.
 * 
 * (In cases when a callback interface is to be passed the proxy type feature is to be used instead - see {@link CoolRMIServiceRegistry})
 * 
 * All used replace entries must be registered on the server and the client side registry!
 */
abstract public class CoolRMIReplaceEntry {
	private Class<?> typeToReplace;
	/**
	 * Create a replace entry for a class or interface to be replaced.
	 * @param typeToReplace this type and all its subclasses are replaced by this entry.
	 */
	public CoolRMIReplaceEntry(Class<?> typeToReplace) {
		super();
		this.typeToReplace = typeToReplace;
	}

	abstract public IReplaceSerializable doReplace(Object o);
	public Class<?> getTypeToReplace() {
		return typeToReplace;
	}
}
