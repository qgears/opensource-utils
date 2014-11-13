package hu.qgears.coolrmi.remoter;

import hu.qgears.coolrmi.CoolRMIService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class CoolRMIServiceRegistry {
	Map<String, CoolRMIService> servicesReg=
		Collections.synchronizedMap(
		new HashMap<String, CoolRMIService>());
	Map<Class<?>, Class<?>> proxyTypes=new HashMap<Class<?>, Class<?>>(); 
	/**
	 * Add a service object for the given name.
	 * It is the user's responsibility to set a service that
	 * implements the required interface. 
	 * 
	 * Synchronized, so should can be called after starting the server.
	 * @param name
	 * @param service
	 */
	public synchronized void addService(CoolRMIService service)
	{
		servicesReg.put(service.getName(), service);
	}
	public synchronized CoolRMIService getService(String name)
	{
		return servicesReg.get(name);
	}

	public synchronized void removeService(String serviceId)
	{
		servicesReg.remove(serviceId);
	}
	public synchronized void addProxyType(Class<?> typeToBeProxied, Class<?> proxyInterface)
	{
		proxyTypes.put(typeToBeProxied, proxyInterface);
	}
	public synchronized Class<?> getProxyType(Class<?> typeToBeProxied)
	{
		return proxyTypes.get(typeToBeProxied);
	}
}
