package hu.qgears.emfcollab.util;

import java.util.HashMap;
import java.util.Map;

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
 * 
 * @author rizsi
 *
 */
public class UUIDXmiResource extends XMIResourceImpl
{
	Map<EObject, String> detachedIds=new HashMap<EObject, String>();
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
			String ret2=detachedIds.get(eObject);
			if(ret2!=null)
			{
				setID(eObject, ret2);
				return ret2;
			}
		}
		return ret;
	}
	@Override
	protected void detachedHelper(EObject eObject) {
		detachedIds.put(eObject, getID(eObject));
		super.detachedHelper(eObject);
	}
}