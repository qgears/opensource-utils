package hu.qgears.commons;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory that sets the name of a
 * thread to something that is usable for the
 * developer.
 * @author rizsi
 *
 */
public class NamedThreadFactory implements ThreadFactory {

	private String name;
	private Integer priority;
	private Boolean daemon;
	
	public NamedThreadFactory(String name) {
		super();
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread ret=new Thread(r, name);
		if(priority!=null)
		{
			ret.setPriority(priority);
		}
		if(daemon!=null)
		{
			ret.setDaemon(daemon);
		}
		return ret;
	}
	/**
	 * Set the priority of the thread to be created by this factory.
	 * @param priority null (default) means not to change default priority
	 * @return self
	 */
	public NamedThreadFactory setPriority(Integer priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Set whether the thread to be created is daemon.
	 * 
	 * @param daemon null(default) means not to change the default value.
	 * @return self
	 */
	public NamedThreadFactory setDaemon(Boolean daemon) {
		this.daemon = daemon;
		return this;
	}

}
