package hu.qgears.emfcollab.srv;

import hu.qgears.emfcollab.exceptions.EmfExceptionAutentication;

import java.io.IOException;


/**
 * Emf server for multiple resource sets.
 * @author rizsi
 *
 */
public interface IEmfMultiServer {
	/**
	 * Create an ID for this user
	 * @return
	 */
	EmfSessionKey login(EmfCredentials credentials) throws EmfExceptionAutentication;
	/**
	 * Get server for a resource by name.
	 * @param resourceName
	 * @return
	 * @throws IOException 
	 */
	IEmfServer getServerForResource(EmfSessionKey sessionKey, EmfCredentials credentials, String resourceName) throws IOException;
}
