package hu.qgears.opengl.commons.container;

import hu.qgears.opengl.commons.AbstractOpenglApplication2;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class OpenGLFrame extends AbstractOpenglApplication2 {

	private static final Logger LOG = Logger.getLogger(OpenGLFrame.class);

	private List<OpenGLAppContainer> containers = new ArrayList<OpenGLAppContainer>();
	private List<OpenGLAppContainer> copyOfContainers=new ArrayList<OpenGLAppContainer>();

	private OpenGLAppContainer current;

	public IOGLContainer createContainer() {
		OpenGLAppContainer ret = new OpenGLAppContainer(this);
		return ret;
	}

	public void addApp(OpenGLAppContainer openGLAppContainer) {
		synchronized (this) {
			containers = new ArrayList<OpenGLAppContainer>(containers);
			containers.add(openGLAppContainer);
			copyOfContainers=null;
			if (current == null) {
				setCurrent(openGLAppContainer);
			}
		}
	}

	public void start() {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				OpenGLFrame.this.run();
			}
		}, "OpenGL");
		// GUI thread gets maximum priority. This hopefully makes effects not
		// frame dropping.
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
	}

	public void run() {
		try {
			execute();
		} catch (Exception e) {
			LOG.error("Error executing OpenGLFrame",e);
		}
		System.exit(0); // NOSONAR intentional return value
	}

	@Override
	protected boolean isDirty() {
		if (current != null) {
			boolean ret = current.isDirty();
			return ret;
		}
		return false;
	}

	@Override
	protected void render() {
		if (current != null) {
			current.render();
		}
	}
	@Override
	protected void logic() {
		List<OpenGLAppContainer> containers=getCopyOfContainers();
		for (int i=0;i<containers.size();++i) {
			OpenGLAppContainer c=containers.get(i);
			try {
				if (!c.isInitialized()) {
					try {
						c.initialize();
					} catch (Exception e) {
						LOG.error("Error initializing container:"+c,e);
					}
					c.setInitialized(true);
				}
				c.logic();
			} catch (Exception e) {
				LOG.error("Error while calling logic of container:"+c,e);
			}
		}
	}

	/*
	 * Suppressing warning, as this method does return either a newly created or 
	 * a cached copy of the 'containers' list.
	 */
	@SuppressWarnings("squid:S2384")
	private List<OpenGLAppContainer> getCopyOfContainers() {
		synchronized (this) {
			if(copyOfContainers==null)
			{
					copyOfContainers=new ArrayList<OpenGLAppContainer>(containers);
			}
			return copyOfContainers;
		}
	}

	@Override
	protected void afterBufferSwap() {
		super.afterBufferSwap();
		if (current != null) {
			current.afterBufferSwap();
		}
	}

	@Override
	protected void beforeBufferSwap() {
		if (current != null) {
			current.beforeBufferSwap();
		}
		super.beforeBufferSwap();
	}

	@Override
	protected void keyDown(int eventKey, char ch, boolean shift, boolean ctrl,
			boolean alt, boolean special) throws Exception {
		if (current != null) {
			current.keyDown(eventKey, ch, shift, ctrl, alt, special);
		}
	}

	public OpenGLAppContainer nextApplication() {
		OpenGLAppContainer ret=null;
		synchronized (this) {
			if(containers.size()>1)
			{
				int idx = containers.indexOf(current);
				if(idx==-1)
				{
					idx=0;
				}else
				{
					idx++;
					int l=containers.size();
					if(l>0)
					{
						idx=idx%l;
					}
				}
				if(idx>=0 &&idx<containers.size())
				{
					ret=containers.get(idx);
					setCurrent(ret);
				}
			}
		}
		return ret;
	}

	private void setCurrent(OpenGLAppContainer curr) {
		synchronized (this) {
			current=curr;
			current.requireRedraw();
			for(OpenGLAppContainer c: containers)
			{
				c.setActive(c==curr);
			}
		}
	}

	public IOGLContainer selectApplication(OpenGLAppContainer openGLAppContainer) {
		setCurrent(openGLAppContainer);
		return openGLAppContainer;
	}

	public void remove(OpenGLAppContainer openGLAppContainer) {
		openGLAppContainer.setActive(false);
		synchronized (this) {
			containers = new ArrayList<OpenGLAppContainer>(containers);
			containers.remove(openGLAppContainer);
			copyOfContainers=null;
			if (openGLAppContainer.equals(current)) {
				current=null;
				nextApplication();
			}
		}
	}

	public List<IOGLContainer> getApplications() {
		synchronized (this) {
			List<IOGLContainer> ret=new ArrayList<IOGLContainer>(containers.size());
			ret.addAll(containers);
			return ret;
		}
	}

	public IOGLContainer getActiveApplication() {
		return current;
	}
}
