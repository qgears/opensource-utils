package hu.qgears.emfcollab;

import hu.qgears.emfcollab.srv.EmfSerializable;

/**
 * Interface for emf events.
 * @author rizsi
 *
 */
public interface EmfEvent extends EmfSerializable {
	EmfEventType getType();
}
