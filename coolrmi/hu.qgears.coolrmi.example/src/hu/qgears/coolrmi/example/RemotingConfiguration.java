package hu.qgears.coolrmi.example;

import java.nio.IntBuffer;

import hu.qgears.coolrmi.remoter.CoolRMIReplaceEntry;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import hu.qgears.coolrmi.remoter.IReplaceSerializable;

public class RemotingConfiguration {
	/**
	 * Object replace entry for replacing non-serializable object through the CoolRMI remoting API
	 * with a serializable object.
	 * 
	 * Any superclass or interface of the actual object is enough to be defined as a replace entry.
	 * 
	 * (In our case this entry will replace all subclasses of IntBuffer: DirectIntBuffer, HeapIntBuffer, etc.)
	 * @author rizsi
	 *
	 */
	static class IntBufferReplace extends CoolRMIReplaceEntry
	{

		public IntBufferReplace() {
			super(IntBuffer.class);
		}

		@Override
		public IReplaceSerializable doReplace(Object o) {
			IntBuffer ib=(IntBuffer) o;
			int[] ints=new int[ib.remaining()];
			ib.get(ints);
			ib.position(ib.position()-ints.length);
			return new IntBufferWrapped(ints);
		}
	}
	/**
	 * This object is replaces itself with the original type of object after it is
	 * de-serialized.
	 * @author rizsi
	 *
	 */
	static class IntBufferWrapped implements IReplaceSerializable
	{
		private static final long serialVersionUID = 1L;
		private int[] ints;
		public IntBufferWrapped(int[] ints) {
			super();
			this.ints = ints;
		}
		@Override
		public Object readResolve() {
			return IntBuffer.wrap(ints);
		}
	}
	/**
	 * Create a remoting configuration that supports a callback type
	 * and a serialization replace type.
	 * If we didn't use these features the configuration could be omitted.
	 * @return
	 */
	public static CoolRMIServiceRegistry createConfiguration() {
		CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
		// Proxy type has to be configured on the side that sends the callback object. (On the client in our case)
		// Only exact matches of this type are proxied (this is a limitation of the current implementation)
		reg.addProxyType(CallbackImpl.class, ICallback.class);
		// Replace type has to be configured on the side that sends the replaced object. (On the client in our case)
		reg.addReplaceType(new IntBufferReplace());
		return reg;
	}
}
