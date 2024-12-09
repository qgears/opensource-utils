package hu.qgears.commons.signal;

import java.util.ArrayList;
import java.util.List;


/**
 * Signal implementation.
 * 
 * This class can be used to fire the signal and
 * also implements the ISignal interface.
 * 
 * TODO make this class thread safe and implement error fencing
 * 
 * @author rizsi
 *
 * @param <T>
 */
public class Signal<T> implements ISignal<T>{
	private List<Slot<T>> slots=new ArrayList<Slot<T>>(2);

	@Override
	public void addSlot(Slot<T> slot) {
		slots.add(slot);
	}

	@Override
	public void removeSlot(Slot<T> slot) {
		slots.remove(slot);
	}
	
	public void doSignal(T value)
	{
		for(Slot<T> slot:new ArrayList<Slot<T>>(slots))
		{
			slot.signal(value);
		}
	}
}
