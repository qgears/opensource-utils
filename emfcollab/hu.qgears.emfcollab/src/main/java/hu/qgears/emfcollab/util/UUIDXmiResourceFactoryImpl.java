package hu.qgears.emfcollab.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * Resource factory that creates UUIDXmiResource
 * @author rizsi
 *
 */
public class UUIDXmiResourceFactoryImpl extends ResourceFactoryImpl {
	@Override
	public Resource createResource(URI uri) {
		return new UUIDXmiResource(uri);
	}
}
