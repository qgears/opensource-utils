package hu.qgears.emfcollab.backref;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;

import hu.qgears.commons.UtilEventListener;

public interface EmfObjectReferences {
	/**
	 * Add a reference value listener to an object.
	 * The listener will be triggered immediately with the current value (notifier is null)
	 * and then on each change.
	 * @param ref
	 * @param event
	 */
	void addReferenceListener(EStructuralFeature ref, UtilEventListener<Notification> event, boolean triggerAtOnce);
}
