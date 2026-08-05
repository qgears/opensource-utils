package hu.qgears.emfcollab.impl;


import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hu.qgears.commons.UtilString;
import hu.qgears.emfcollab.exceptions.EmfExceptionAutentication;
import hu.qgears.emfcollab.exceptions.EmfExceptionSession;
import hu.qgears.emfcollab.srv.EmfCredentials;
import hu.qgears.emfcollab.srv.EmfSessionId;
import hu.qgears.emfcollab.srv.EmfSessionKey;
import hu.qgears.emfcollab.srv.IEmfMultiServer;
import hu.qgears.emfcollab.srv.IEmfServer;

public class EmfMultiServer implements IEmfMultiServer {
	private Map<String, EmfSession> sessionMap=new HashMap<String, EmfSession>();
	private SecureRandom secureRandom=new SecureRandom();
	Random sessionIdCounter=new Random();
	private IResourceLoader resourceLoader;
	class ServerListener implements IEmfServerListener
	{
		String resourceName;
		public ServerListener(String resourceName) {
			super();
			this.resourceName = resourceName;
		}
		@Override
		public void save(ResourceWithHistory loadedResource,
				EmfCredentials credentials,
				String commitLog) throws IOException {
			resourceLoader.saveResource(credentials, resourceName,
					loadedResource,
					commitLog);
		}
		@Override
		public void commit(ResourceWithHistory loadedResource,
				EmfCredentials credentials, String commitLog) throws IOException {
			resourceLoader.commitResource(credentials, resourceName,
					loadedResource,
					commitLog);
		}
		@Override
		public void revert(LoadedResource loadedResource) {
			resourceLoader.revertResource(resourceName);
		}
	}
	public EmfMultiServer(IResourceLoader resourceLoader) {
		super();
		this.resourceLoader = resourceLoader;
	}
	public synchronized void init(IResourceLoader resourceLoader) throws IOException
	{
		this.resourceLoader=resourceLoader;
	}
	private Map<String, EmfServer> servers=new HashMap<String, EmfServer>();
	@Override
	public synchronized IEmfServer getServerForResource(
			EmfSessionKey sessionKey,
			EmfCredentials credentials,
			String resourceName) throws IOException {
		checkSessionKey(sessionKey);
		EmfServer ret=servers.get(resourceName);
		if(ret==null)
		{
			ret=new EmfServer(this, resourceName, resourceLoader.getLogFile(resourceName));
			ResourceWithHistory r=resourceLoader.loadResource(credentials, resourceName);
			ret.init(r, new ServerListener(resourceName));
			servers.put(resourceName, ret);
		}
		return ret;
	}
	public synchronized EmfSession checkSessionKey(EmfSessionKey sessionKey) {
		EmfSession session=sessionMap.get(sessionKey.getSessionKey());
		if(session==null)
		{
			throw new EmfExceptionSession();
		}
		return session;
	}
	@Override
	public synchronized EmfSessionKey login(EmfCredentials credentials)
			throws EmfExceptionAutentication {
		resourceLoader.authenticate(credentials);
		byte[] seed=secureRandom.generateSeed(32);
		String sessionId=UtilString.toHex(seed);
		EmfSessionId clientId=new EmfSessionId(Math.abs(sessionIdCounter.nextLong()));
		clientId.setUserName(credentials.getUserName());
		EmfSessionKey ret=new EmfSessionKey(sessionId, clientId);
		sessionMap.put(sessionId,
				new EmfSession(credentials.getUserName(), clientId));
		return ret;
	}
	public synchronized void serverDisposed(EmfServer srv)
	{
		servers.remove(srv.getResoruceName());		
	}
}
