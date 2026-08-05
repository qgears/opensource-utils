package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.exceptions.EmfExceptionAutentication;
import hu.qgears.emfcollab.srv.EmfCredentials;

import java.io.File;
import java.io.IOException;



/**
 * The resource loader maps a resource name to a resource set.
 * 
 * This interface is used by the EMF server to load resources when a client
 * requires the usage of the resource.
 * 
 * @author rizsi
 *
 */
public interface IResourceLoader {
	void authenticate(EmfCredentials credentials) throws EmfExceptionAutentication;

	ResourceWithHistory loadResource(EmfCredentials credentials, String resourceName) throws IOException;

	void saveResource(EmfCredentials credentials, String resourceName, ResourceWithHistory resource, String commitLog) throws IOException;

	void commitResource(EmfCredentials credentials, String resourceName,
			ResourceWithHistory loadedResource, String commitLog) throws IOException;

	File getLogFile(String resourceName);

	void revertResource(String resourceName);
}
