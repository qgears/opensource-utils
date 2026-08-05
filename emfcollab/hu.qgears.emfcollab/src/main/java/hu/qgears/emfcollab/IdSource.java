package hu.qgears.emfcollab;

import org.eclipse.emf.ecore.EObject;

public interface IdSource {
	String getId(EObject eobject);

	EObject getById(String id);

	void setId(EObject created, String createdId);
}
