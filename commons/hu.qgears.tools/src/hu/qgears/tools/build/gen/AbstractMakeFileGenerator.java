package hu.qgears.tools.build.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilString;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.compiler.CompileCommandBuilder;

abstract public class AbstractMakeFileGenerator extends RAbstractTemplatePart{
	protected BuildGenContext bgc;
	public AbstractMakeFileGenerator(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
		this.bgc=codeGeneratorContext;
	}
	private List<String> allTargets=new ArrayList<>();
	private void printTargets(Object[] o)
	{
		// BundleManifest src: bgc.r.allTobuildBundles.getAll()
		for(String tg:allTargets)
		{
			write(" ");
			writeObject(tg);
		}
		write("\n");
	}
	final public void generate() throws Exception
	{
		write("all:");
		deferred(this::printTargets, allTargets);
		write("\n\n");
		generateTargets();
		finishCodeGeneration("Makefile");
	}
	final protected void addTarget(String target) {
		allTargets.add(target);
	}
	abstract protected void generateTargets() throws Exception;
	final protected String getBuildTargetJar(BundleManifest src) {
		return src.id+".jar";
	}
	final protected String getBuildTargetUberJar(BundleManifest src) {
		return src.id+".uber.jar";
	}

	final protected List<String> findClassNames(BundleManifest src) {
		List<String> collected=new ArrayList<>();
		for(String p: src.cph.sourceFolders)
		{
			File f=new File(src.projectFile, p);
			try {
				new UtilFileVisitor()
				{
					@Override
					protected boolean visited(File dir, String localPath) throws Exception {
						if(dir.isFile() && localPath.endsWith(".java"))
						{
							collected.add(dir.getAbsolutePath());
//							collected.add(localPath.substring(0, localPath.length()-5).replaceAll("\\/", "\\."));
						}
						return super.visited(dir, localPath);
					}
				}
				.visit(f);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return collected;
	}

	final protected List<String> getClassPathFolders(BundleManifest dep, boolean self) {
		List<String> ret=new ArrayList<>();
		switch (dep.type) {
		case binary:
			ret.add(dep.projectFile.getAbsolutePath());
			for(String jar: dep.bundleClasspath)
			{
				ret.add(dep.projectFile.getAbsolutePath()+"/"+jar);
			}
			break;
		case source:
			if(self)
			{
				for(String path: dep.cph.sourceFolders)
				{
					ret.add(dep.projectFile.getAbsolutePath()+"/"+path);
				}
			}else
			{
				ret.add(getBuildTargetJar(dep));
			}
			for(String path: dep.cph.libFolders)
			{
				if(path.startsWith("/"))
				{
					ret.add(path);
				}else
				{
					ret.add(dep.projectFile.getAbsolutePath()+"/"+path);
				}
			}
			for(String id: dep.cph.userLibraries)
			{
				for(File f: bgc.args.userLibraries)
				{
					File g=new File(f, id);
					if(g.exists() && g.isDirectory())
					{
						for(File lib: UtilFile.listFiles(g))
						{
							if(lib.isDirectory())
							{
								ret.add(lib.getAbsolutePath());
							}else if(lib.isFile() && lib.getName().endsWith(".jar"))
							{
								ret.add(lib.getAbsolutePath());
							}
						}
					}
				}
			}
			for(String jar: dep.bundleClasspath)
			{
				ret.add(dep.projectFile.getAbsolutePath()+"/"+jar);
			}
			break;
		case dummy:
			break;
		default:
			throw new IllegalArgumentException();
		}
		return ret;
	}
	final protected List<String> getSourceFolders(BundleManifest src) {
		List<String> ret=new ArrayList<>();
		for(String path: src.cph.sourceFolders)
		{
			ret.add(src.projectFile.getAbsolutePath()+"/"+path);
		}
		return ret;
	}
	protected void generateCompileCommand(CompileCommandBuilder compileCommand)
	{
		if(compileCommand.outputRelativePath==null)
		{
			throw new IllegalArgumentException();
		}
		List<String> allDeps=new ArrayList<>(compileCommand.binaryDeps);
		allDeps.addAll(compileCommand.sourceDeps);
		write("\tjavac -cp ");
		writeObject(UtilString.concat(allDeps, "\\\n:"));
		write(" ");
		writeObject(UtilString.concat(new ArrayList<>(compileCommand.classesToCompile), " \\\n"));
		write(" \\\n-d ");
		writeObject(compileCommand.outputRelativePath);
		write("\n");
	}
}
