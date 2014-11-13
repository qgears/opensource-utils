package hu.qgears.emfcollab;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public interface IdSource {
	String getId(Resource resource, EObject eobject);

	EObject getById(Resource resource, String id);

	void setId(Resource resource, EObject created, String createdId);
}
