package hu.qgears.emfcollab;

import hu.qgears.emfcollab.util.IdAdapter;
import hu.qgears.emfcollab.util.UUIDXmiResource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;


public class XmiIdSource implements IdSource {
	private ResourceSet rs;
	public XmiIdSource(ResourceSet rs) {
		super();
		this.rs = rs;
	}
	@Override
	public String getId(EObject eobject) {
		Resource resource=eobject.eResource();
		String uuid;
		if(resource!=null)
		{
			final UUIDXmiResource res=(UUIDXmiResource) resource;
			uuid=res.getID(eobject);
		}else
		{
			uuid=IdAdapter.get(eobject).getId();
		}
		if(uuid==null)
		{
			uuid=EcoreUtil.generateUUID();
			setId(eobject, uuid);
		}
		return uuid;
	}
	@Override
	public EObject getById(String id) {
		for(Resource r: rs.getResources())
		{
			final XMIResource res=(XMIResource) r;
			EObject ret=res.getEObject(id);
			if(ret!=null)
			{
				return ret;
			}
		}
		return null;
	}
	@Override
	public void setId(EObject created, String createdId) {
		IdAdapter.get(created).setId(createdId);
		Resource r=created.eResource();
		if(r!=null)
		{
			final XMIResource res=(XMIResource) r;
			res.setID(created, createdId);
		}
	}
}
