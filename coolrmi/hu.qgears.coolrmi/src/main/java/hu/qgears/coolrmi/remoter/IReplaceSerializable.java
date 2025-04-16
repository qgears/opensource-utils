package hu.qgears.coolrmi.remoter;

import java.io.Serializable;

/**
 * This interface must be implemented by serialization replacer objects.
 * See {@link CoolRMIReplaceEntry}
 * @author rizsi
 *
 */
public interface IReplaceSerializable extends Serializable {
	/**
	 * This method is called after deserialization to create the equivalent of the original object.
	 * @return
	 */
	Object readResolve();

}
