package hu.qgears.tools.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSkip;
import joptsimple.tool.AbstractTool.IArgs;

public class Args implements IArgs
{
	public Args() {
	}
	/**
	 * Recursively visited for source projects.
	 * All projects with a launcher.txt generate a runnable jar.
	 */
	public List<File> sourceFolders=new ArrayList<>();
	public List<String> ignoreSourceProject=new ArrayList<>();
	public List<String> ignoreBinaryProject=new ArrayList<>();
	/**
	 * Configurable when executed from program.
	 * Maps the output files to be put to a different folder instead of the 
	 * default one in the build folder.
	 * Only used when directbuild is used.
	 */
	@JOSkip
	public Map<String, IFileOutput> outputMapper=new HashMap<>();
	@JOSkip
	public IBuildExtensions buildExtensions=new NullBuildExtensions();
	public List<File> pools=new ArrayList<>();
	@JOHelp("Folder where org.eclipse.jdt.USER_LIBRARY/VARNAME libraries are stored. Folder VARNAME is searched in these folders and its contents are used as build time dependencies.")
	public List<File> userLibraries=new ArrayList<File>();
	
	@JOHelp("Folder where Makefile and other artifacts are generated. Build is executed here.")
	public File out;
	
	@JOSkip
	public Date authorDate=new Date();
	@Override
	public void validate() {
		if(out==null)
		{
			throw new IllegalArgumentException("out must be specified");
		}
	}
}
