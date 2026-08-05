package hu.qgears.emfcollab.backref;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/** 
 * Interface for storing source, refType, target trio.
 */
public interface EmfReference {
	EObject getSource();
	EReference getRefType();
	EObject getTarget();
}
