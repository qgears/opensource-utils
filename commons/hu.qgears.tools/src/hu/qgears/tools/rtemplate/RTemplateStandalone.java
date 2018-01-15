package hu.qgears.tools.rtemplate;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import hu.qgears.commons.UtilFile;
import hu.qgears.rtemplate.RTemplate;
import hu.qgears.rtemplate.TemplateSequences;
import hu.qgears.rtemplate.task.FileVisitor;
import joptsimple.tool.AbstractTool;

public class RTemplateStandalone extends AbstractTool {

	@Override
	public String getId() {
		return "rtemplate";
	}

	@Override
	public String getDescription() {
		return "RTemplate standalone program for usage outside Eclipse";
	}
	public class Args implements IArgs
	{
		public File src;
		public File template;
		public File conf;
		@Override
		public void validate() {
			if(src==null)
			{
				throw new IllegalArgumentException("src folder must not be null");
			}
			if(template==null)
			{
				throw new IllegalArgumentException("template folder must not be null");
			}
		}
	}
	@Override
	protected int doExec(IArgs a) throws Exception {
		final Args arg=(Args) a;
		Properties ps=new Properties();
		if(arg.conf!=null)
		{
			try(FileInputStream fis=new FileInputStream(arg.conf))
			{
				ps.load(fis);
			}
		}
		final TemplateSequences ts=TemplateSequences.parseProperties(ps);
		final RTemplate rt=new RTemplate(ts);
		new FileVisitor()
		{
			@Override
			public void visit(File f, String prefix) {
				System.out.println("nput file: "+f+" '"+prefix+"'");
				if(ts.fileNameMatches(f))
				{
					System.out.println("Transforming file: "+f);
					try {
						String s=rt.javaToTemplate(UtilFile.loadAsString(f));
						File g=new File(arg.template, prefix+f.getName()+".rtemplate");
						g.getParentFile().mkdirs();
						UtilFile.saveAsFile(g, s);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.visitDir(arg.src, "");
		ChangeTracker s=new ChangeTracker(arg.src){
			@Override
			protected void changed(File f, String prefix) {
				transform(f, prefix);
			}
			protected void transform(File f, String prefix) {
				if(ts.fileNameMatches(f))
				{
				System.out.println("Transforming file: "+f);
				try {
					String s=rt.javaToTemplate(UtilFile.loadAsString(f));
					File g=new File(arg.template, prefix+f.getName()+".rtemplate");
					g.getParentFile().mkdirs();
					UtilFile.saveAsFile(g, s);
					other.skiponce(g);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
			@Override
			protected void inited(File f, String prefix) {
				transform(f, prefix);
			}
		};
		ChangeTracker t=new ChangeTracker(arg.template)
		{
			@Override
			protected void inited(File f, String prefix) {
			}
			@Override
			protected void changed(File f, String prefix) {
				if(f.getName().endsWith(".rtemplate"))
				{
					String n=f.getName();
					String newName=n.substring(0,n.length()-".rtemplate".length());
					try {
						String s=rt.templateToJava(UtilFile.loadAsString(f));
						File g=new File(arg.src, prefix+newName);
						System.out.println("Transforming file: "+f+" newname: "+newName+" "+g.getAbsolutePath());
						g.getParentFile().mkdirs();
						UtilFile.saveAsFile(g, s);
						other.skiponce(g);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		t.other=s;
		s.other=t;
		while(true)
		{
			s.run();
			t.run();
			Thread.sleep(1000);
		}
	}

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}


}
