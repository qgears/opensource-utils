package hu.qgears.tools.rtemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hu.qgears.rtemplate.task.FileVisitor;

public class ChangeTracker extends FileVisitor
{
	class C
	{
		public long lm;
	}
	private Map<String, C> changes=new HashMap<>();
	private File folder;
	private boolean firstRun=true;
	protected ChangeTracker other;
	private Set<String> toSkip=new HashSet<>();
	
	public ChangeTracker(File folder) {
		super();
		this.folder = folder;
	}
	public void run() {
		visitDir(folder, "");
		firstRun=false;
	}
	@Override
	public void visit(File f, String prefix) {
		String fname=prefix+f.getName();
		C c=changes.get(fname);
		long lm=f.lastModified();
		boolean changed=false;
		if(c==null)
		{
			c=new C();
			c.lm=lm;
			changes.put(fname, c);
			changed=true;
		}
		if(c.lm<lm)
		{
			changed=true;
			c.lm=lm;
		}
		if(changed)
		{
			try {
				if(toSkip.contains(f.getCanonicalPath()))
				{
					// Do not process
					toSkip.remove(f.getCanonicalPath());
				}else
				{
					process(f, prefix);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void process(File f, String prefix) {
		if(firstRun)
		{
			inited(f, prefix);
		}else
		{
			changed(f, prefix);
		}
	}
	protected void inited(File f, String prefix) {
	}
	protected void changed(File f, String prefix) {
	}
	public void skiponce(File g) throws IOException {
		toSkip.add(g.getCanonicalPath());
	}

}
