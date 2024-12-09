package hu.qgears.commons;


/**
 * Disposeable that has a reference counter implemented:
 *  * methods that pass such an obejct must document whether the object's ownership is passed or not
 *  * by default methods with prefix create, grab, allocate pass the owhership (increment the reference count)
 *  * reference counter can be incremented or decremented
 *  * when reference count reaches 0 the object must call its own dispose method (from the release() method)
 * @author rizsi
 *
 */
public interface IReferenceCountedDisposeable extends IDisposeable {
	/**
	 * Increment the reference counter.
	 * When reference counter is zero then dispose is called
	 * 
	 * Reference counter must be implemented either volatile or synchronised!
	 */
	void incrementReferenceCounter();
	/**
	 * Decrement the reference counter.
	 * When reference counter is zero then dispose is called
	 * 
	 * Reference counter must be implemented either volatile or synchronised!
	 */
	void decrementReferenceCounter();
}
