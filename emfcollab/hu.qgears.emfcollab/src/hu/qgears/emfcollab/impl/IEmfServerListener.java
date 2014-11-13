package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.srv.EmfCredentials;

import java.io.IOException;


public interface IEmfServerListener {
	void save(ResourceWithHistory loadedResource, EmfCredentials credentials, String commitLog) throws IOException;

	void commit(ResourceWithHistory loadedResource, EmfCredentials credentials,
			String commitLog) throws IOException;

//	void revert(ResourceWithHistory loadedResource);

	void revert(LoadedResource loadedResource);
}
