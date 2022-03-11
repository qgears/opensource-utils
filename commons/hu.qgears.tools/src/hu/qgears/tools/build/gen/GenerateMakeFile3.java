package hu.qgears.tools.build.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.Resolver;

/**
 * A variant that only compiles each packages but does not
 * copy resources and create uberjars.
 * Only to compare speed to {@link GenerateMakeFile2}.
 * TODO delete
 * @author rizsi
 *
 */
public class GenerateMakeFile3 extends RAbstractTemplatePart{
	protected BuildGenContext bgc;
	public GenerateMakeFile3(BuildGenContext codeGeneratorContext) {
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
	public void generate()
	{
		write("all:");
		deferred(this::printTargets, allTargets);
		write("\n");
		for(BundleManifest src: bgc.r.allTobuildBundles.getAll())
		{
			genCompile(src);
		}
		finishCodeGeneration("Makefile");
	}

	private void genCompile(BundleManifest src) {
		BundleSet bs=new BundleSet();
		BundleSet resultBundles=new BundleSet();
		bs.add(src);
		Resolver e=new Resolver(bs, bgc.r.getResultBundlesAsBundleSet(), resultBundles);
		e.resolve();
		List<BundleManifest> bundles=resultBundles.getAll();
		bundles.remove(src);
		List<BundleManifest> bundlesAndSelf=resultBundles.getAll();
		String target=getBuildTargetJar(src);
		allTargets.add(target);
		writeObject(target);
		write(":");
		for(BundleManifest dep: bundles)
		{
			write(" ");
			switch (dep.type) {
			case binary:
			writeObject(dep.projectFile.getAbsolutePath());
				break;
			case dummy:
				break;
			case source:
			writeObject(getBuildTargetJar(dep));
				break;
			default:
				throw new RuntimeException("Type not known");
			}
		}
		String outFolder=new File(bgc.out, src.id).getAbsolutePath();
		UtilComma depsComma=new UtilComma(":");
		write("\n\trm -rf ");
		writeObject(outFolder);
		write("\n\tmkdir -p ");
		writeObject(outFolder);
		write("\n");
		for(String folder: getSourceFolders(src))
		{
			// TODO only copy non- .java files
			write("#\tcp -r ");
			writeObject(folder);
			write("/* ");
			writeObject(outFolder);
			write("\n");
		}
		write("\tjavac -cp ");
		for(BundleManifest dep:bundlesAndSelf)
		{
			for(String classPathFolder: getClassPathFolders(dep, dep==src))
			{
				writeObject(depsComma.getSeparator());
				writeObject(classPathFolder);
			}
		}
		List<String> classes=findClassNames(src);
		for(String cla: classes)
		{
			write(" ");
			writeObject(cla);
		}
		write(" -d ");
		writeObject(outFolder);
		write("\n\trm -f ");
		writeObject(src.id);
		write(".jar\n\tcd ");
		writeObject(outFolder);
		write("; zip -r ../");
		writeObject(src.id);
		write(".jar .\n\n\n");
	}
	private String getBuildTargetJar(BundleManifest src) {
		return src.id+".jar";
	}

	private List<String> findClassNames(BundleManifest src) {
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

	private List<String> getClassPathFolders(BundleManifest dep, boolean self) {
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
				ret.add(dep.projectFile.getAbsolutePath()+"/"+path);
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
	private List<String> getSourceFolders(BundleManifest src) {
		List<String> ret=new ArrayList<>();
		for(String path: src.cph.sourceFolders)
		{
			ret.add(src.projectFile.getAbsolutePath()+"/"+path);
		}
		return ret;
	}
}
