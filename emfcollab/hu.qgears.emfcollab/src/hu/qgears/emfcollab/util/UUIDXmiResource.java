package hu.qgears.emfcollab.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

/**
 * Xmi resource that requests references to be set by UUID.
 * 
 * This resource remembers UUIDs after deletion. This is a must for emfcollab.
 * 
 * TODO - objects are stored "forever" this prevents them from being garbage collected.
 * A mechanism to know when they are no longer needed should be implemented. 
 */
public class UUIDXmiResource extends XMIResourceImpl
{
	public UUIDXmiResource() {
		super();
	}

	public UUIDXmiResource(URI uri) {
		super(uri);
	}

	@Override
	protected boolean useUUIDs() {
		return true;
	}
	@Override
	public String getID(EObject eObject) {
		String ret=super.getID(eObject);
		if(ret==null)
		{
			IdAdapter ad=IdAdapter.get(eObject);
			return ad.getId();
		}
		return ret;
	}
	@Override
	public void setID(EObject eObject, String id) {
		IdAdapter ad=IdAdapter.get(eObject);
		ad.setId(id);
		super.setID(eObject, id);
	}
	@Override
	protected void detachedHelper(EObject eObject) {
		super.detachedHelper(eObject);
	}
}