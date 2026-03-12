package hu.qgears.coolrmi.streams;

import java.util.HashMap;
import java.util.Map;

import hu.qgears.coolrmi.ICoolRMILogger;

public class ConnectionConfiguration {
	private ICoolRMILogger log=new ICoolRMILogger() {
		@Override
		public void logError(Throwable e) {
			System.err.println("ConnectionConfiguration error log:");
			e.printStackTrace();
		}
	};
	private Map<String, Object> userData=new HashMap<>();
	public Object setUserData(String key, Object value)
	{
		synchronized (userData) {
			return userData.put(key, value);
		}
	}
	public Object getUserData(String key)
	{
		synchronized (userData) {
			return userData.get(key);
		}
	}
	public ICoolRMILogger getLog() {
		return log;
	}
	public void setLog(ICoolRMILogger log) {
		this.log = log;
	}
}
