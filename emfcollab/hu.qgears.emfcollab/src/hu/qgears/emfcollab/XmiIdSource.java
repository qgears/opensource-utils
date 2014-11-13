package hu.qgears.emfcollab;

import hu.qgears.emfcollab.util.UUIDXmiResource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;



public class XmiIdSource implements IdSource {

	@Override
	public String getId(Resource resource, EObject eobject) {
		final UUIDXmiResource res=(UUIDXmiResource) resource;
		String uuid;
		if((uuid=res.getID(eobject))==null)
		{
			uuid=EcoreUtil.generateUUID();
			res.setID(eobject, uuid);
		}
		return uuid;
	}

	@Override
	public EObject getById(Resource resource, String id) {
		final XMIResource res=(XMIResource) resource;
		EObject ret=res.getEObject(id);
		return ret;
	}

	@Override
	public void setId(Resource resource, EObject created, String createdId) {
		final XMIResource res=(XMIResource) resource;
		res.setID(created, createdId);
	}

}
