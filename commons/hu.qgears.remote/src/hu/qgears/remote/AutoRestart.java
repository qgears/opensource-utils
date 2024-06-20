package hu.qgears.remote;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import joptsimple.annot.JONonOptionArgumentsList;
import joptsimple.annot.JOSkip;
import joptsimple.tool.AbstractTool;

public class AutoRestart extends AbstractTool {
	public static class Args implements IArgs {
		@JOSkip
		public File current;
		public File folder;
		public File args;
		public String className;
		@JONonOptionArgumentsList
		public List<String> others=new ArrayList<>();
		
		public File newFile()
		{
			if(current!=null)
			{
				String name=current.getName();
				String basename=name.substring(0, name.length()-".jar".length());
				int ndigit=0;
				while(Character.isDigit(basename.charAt(basename.length()-ndigit-1)))
				{
					ndigit++;
				}
				System.out.println("ngidit: "+ndigit);
				String bbname;
				int index;
				if(ndigit!=10)
				{
					bbname=basename+"X";
					index=1;
				}else
				{
					bbname=basename.substring(0,basename.length()-10);
					
					index=Integer.parseInt(basename.substring(basename.length()-10));
					index++;
				}
				String newName=bbname+UtilString.fillLeft(""+index, 10, '0')+".jar";
				return new File(current.getParentFile(), newName);
			}
			return null;
		}
		
		@Override
		public void validate() {
		}
	}
	public static Args autoRestartArgs;
	Args a;

	@Override
	public String getId() {
		return "autorestart";
	}

	@Override
	public String getDescription() {
		return "Dynamically load a jar file, execute it and restart when it returns";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		this.a=(Args)a;
		autoRestartArgs=this.a;
		while (true) {
			executeOnce();
		}
	}

	private void executeOnce() throws InterruptedException {
		boolean runned=false;
		TreeMap<String, File> delegates=new TreeMap<>();
		for (File f : UtilFile.listFiles(a.folder)) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				delegates.put(f.getName(), f);
			}
		}
		if(delegates.size()>0)
		{
			System.out.println(""+delegates);
			File found=delegates.lastEntry().getValue();
			runned=true;
			try {
				URLClassLoader child = new URLClassLoader(new URL[] { found.toURI().toURL() },
						null);
				Class<?> classToLoad = Class.forName(a.className, true, child);
				Method method = classToLoad.getDeclaredMethod("autoRestartEntry", File.class, String[].class);
				String[] args=a.others.toArray(new String[] {});
				autoRestartArgs.current=found;
				System.out.println("Start program: "+found+" "+method);
				for(String a: args)
				{
					System.out.println("arg: "+a);
				}
				System.out.println("next file to update: "+autoRestartArgs.newFile());
				method.invoke(null, autoRestartArgs.current, (Object)args);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if(!runned)
		{
			System.err.println("Executable not found in folder: "+a.folder);
		}
		System.err.println("Process exited... restart after 1s wait...");
		Thread.sleep(1000);
	}

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
}
