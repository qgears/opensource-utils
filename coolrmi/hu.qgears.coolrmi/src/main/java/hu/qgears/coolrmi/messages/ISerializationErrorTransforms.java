package hu.qgears.coolrmi.messages;

import java.io.ObjectStreamException;

/** If serialization throws an exception of this type then CoolRMI can replace an object with a different object when serialized.
 */
public interface ISerializationErrorTransforms {
	void serializationError(ObjectStreamException e);
}
