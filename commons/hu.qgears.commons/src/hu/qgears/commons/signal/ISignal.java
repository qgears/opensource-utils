package hu.qgears.commons.signal;


/**
 * A signal that can be "listened" using a slot.
 * 
 * The interface can only be used to listen the
 * signal but can not fire it.
 * 
 * @author rizsi
 *
 * @param <T>
 */
public interface ISignal<T>
{
	/**
	 * Add a signal listener slot to the signal.
	 * @param slot
	 */
	void addSlot(Slot<T> slot);
	/**
	 * Remove a signal listener slot from the signal.
	 * @param slot
	 */
	void removeSlot(Slot<T> slot);
}
