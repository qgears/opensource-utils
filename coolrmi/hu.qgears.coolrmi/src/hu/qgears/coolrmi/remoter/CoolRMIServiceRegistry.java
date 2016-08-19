package hu.qgears.coolrmi.remoter;

import hu.qgears.coolrmi.CoolRMIService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Service registry maps names to service objects.
 * 
 * It also stores proxy types. Proxy types are classes which not serialized but a proxy object 
 * is sent instead which allows callback on the original object through the remoting channel.
 * 
 * It also stores {@link CoolRMIReplaceEntry}s.
 * 
 * TODO configuration should not be changed after using the first service.
 *  (This is a limitation of the current implementation because it does not clear its caches on changes.)
 * 
 * @author rizsi
 *
 */
public class CoolRMIServiceRegistry {
	Map<String, CoolRMIService> servicesReg=
		Collections.synchronizedMap(
		new HashMap<String, CoolRMIService>());
	Map<Class<?>, Class<?>> proxyTypes=new HashMap<Class<?>, Class<?>>();
	Map<Class<?>, CoolRMIReplaceEntry> replaceTypes=new HashMap<Class<?>, CoolRMIReplaceEntry>();
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
	/**
	 * Register a proxy (callback) type on the interface.
	 * @param typeToBeProxied objects that are exactly this class are not sent directly to the other side but as a proxy.
	 * @param proxyInterface Methods on this interface are possible to be sent through the created proxy object. This interface should be used as the type of argument on the remoting API.
	 */
	public synchronized void addProxyType(Class<?> typeToBeProxied, Class<?> proxyInterface)
	{
		proxyTypes.put(typeToBeProxied, proxyInterface);
	}
	/**
	 * Get the registered proxy type for this class.
	 * @param typeToBeProxied
	 * @return null if there is no proxy registry enrty for this class.
	 */
	public synchronized Class<?> getProxyType(Class<?> typeToBeProxied)
	{
		return proxyTypes.get(typeToBeProxied);
	}
	public synchronized void addReplaceType(CoolRMIReplaceEntry replaceEntry)
	{
		CoolRMIReplaceEntry prev=replaceTypes.put(replaceEntry.getTypeToReplace(), replaceEntry);
		if(prev!=null)
		{
			throw new RuntimeException("Replace type was already registered.");
		}
	}
	/**
	 * Replace the object just before serialization to an other type.
	 * See {@link CoolRMIReplaceEntry}
	 * @param obj
	 * @return
	 */
	public Object replaceObject(Object obj)
	{
		CoolRMIReplaceEntry replace=replaceTypes.get(obj.getClass());
		if(replace==null)
		{
			return null;
		}
		return replace.doReplace(obj);
	}
	/**
	 * In case the class is not found in the map we traverse each superclasses
	 * to find if it has a replace object entry.
	 * If it has then replace the object.
	 * If it does not have then store the fact that this class is not replaced
	 * so the next time we don't need to traverse all superclasses.
	 * @param obj
	 * @return
	 */
	public Object replaceObjectHeavy(Object obj) {
		CoolRMIReplaceEntry found;
		if(replaceTypes.containsKey(obj))
		{
			found=replaceTypes.get(obj);
		}
		else
		{
			found=null;
			for(CoolRMIReplaceEntry e: replaceTypes.values())
			{
				if(e.getTypeToReplace().isAssignableFrom(obj.getClass()))
				{
					found=e;
					break;
				}
			}
			replaceTypes.put(obj.getClass(), found);
			
		}
		if(found==null)
		{
			return null;
		}
		return found.doReplace(obj);
	}
}
