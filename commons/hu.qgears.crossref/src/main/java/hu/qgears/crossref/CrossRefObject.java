package hu.qgears.crossref;

import java.util.HashMap;
import java.util.Map;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;

abstract public class CrossRefObject implements NoExceptionAutoClosable {
	private CrossRefManager host;
	private UtilEvent<CrossRefObject> onClose=new UtilEvent<>();
	private Map<String, Object> userObjects=new HashMap<>();
	private boolean closed=false;
	public CrossRefObject(CrossRefManager host) {
		super();
		this.host = host;
	}
	public UtilEvent<CrossRefObject> getOnClose() {
		return onClose;
	}
	@Override
	public void close() {
		if(!closed)
		{
			closed=true;
			onClose.eventHappened(this);
			host.closed(this);
		}
	}
	public CrossRefManager getHost() {
		return host;
	}
	public Object getUserObject(String key)
	{
		return userObjects.get(key);
	}
	public CrossRefObject setUserObject(String key, Object value)
	{
		userObjects.put(key, value);
		return this;
	}
	public boolean isClosed() {
		return closed;
	}
	abstract public Doc getDoc();
}
