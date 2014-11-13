package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.IdSource;

import org.eclipse.emf.ecore.resource.Resource;


/**
 * All files of the resource set must be loaded when returned.
 * 
 * Dynamic loading of resources is not supported by EMFCollab.
 */
public class LoadedResource {
	private Resource resource;
	public LoadedResource(Resource resource, IdSource idSoruce) {
		super();
		this.resource = resource;
		this.idSoruce = idSoruce;
	}
	public Resource getResource() {
		return resource;
	}
	public IdSource getIdSoruce() {
		return idSoruce;
	}
	private IdSource idSoruce;
}
