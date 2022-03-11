package hu.qgears.tools.build.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.qgears.tools.build.BinFirstSorter;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.LauncherData;
import hu.qgears.tools.build.Resolver;
import hu.qgears.tools.build.compiler.CompileCommandBuilder;

/**
 * Do not generate intermediate representations, only applications with main.
 * 
 * For each application:
 *  *collect all sources and deps.
 *  * Compile them in one run
 *  * Copy all resources, sources and simple Manifest into a single JAR file
 *  
 * @author rizsi
 *
 */
public class GenerateMakeFile4 extends AbstractMakeFileGenerator{
	public GenerateMakeFile4(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
	}
	@Override
	protected void generateTargets() {
		for(BundleManifest src: bgc.r.allTobuildBundles.getAll())
		{
			if(src.launchers.size()>0)
			{
				new GenLauncher().genLaunchers(src, src.launchers);
			}
		}
	}

	private class GenLauncher
	{
		CompileCommandBuilder compileCommand=new CompileCommandBuilder();
		public void genLaunchers(BundleManifest src, List<LauncherData> launchers) {
			BundleSet bs=new BundleSet();
			BundleSet resultBundles=new BundleSet();
			bs.add(src);
			Resolver e=new Resolver(bs, bgc.r.getResultBundlesAsBundleSet(), resultBundles);
			e.resolve();
			List<BundleManifest> bundlesAndSelf=e.getBundlesInDependencyOrder();
			Collections.sort(bundlesAndSelf, new BinFirstSorter());
			List<BundleManifest> bundles=new ArrayList<>(bundlesAndSelf);
			bundles.remove(src);
			for(BundleManifest dep:bundlesAndSelf)
			{
				switch(dep.type)
				{
				case binary:
					compileCommand.binaryDeps.addAll(getClassPathFolders(dep, true));
					break;
				case source:
					compileCommand.sourceDeps.addAll(getClassPathFolders(dep, true));
					compileCommand.classesToCompile.addAll(findClassNames(dep));
					break;
				case dummy:
					break;
				default:
					throw new RuntimeException();
				}
			}
			String target=getBuildTargetUberJar(src);
			compileCommand.outputRelativePath=target+".temp";
			addTarget(target);
			writeObject(target);
			write(":\n\tmkdir -p ");
			writeObject(compileCommand.outputRelativePath);
			write("\n");
			generateCompileCommand(compileCommand);
			write("\tcd ");
			writeObject(compileCommand.outputRelativePath);
			write("; zip -ro ../");
			writeObject(target);
			write(" .\n\n");
		}
	}
	@SuppressWarnings("unused")
	private void genLauncher(BundleManifest src, LauncherData l) {
		BundleSet bs=new BundleSet();
		BundleSet resultBundles=new BundleSet();
		bs.add(src);
		Resolver e=new Resolver(bs, bgc.r.getResultBundlesAsBundleSet(), resultBundles);
		e.resolve();
		List<BundleManifest> bundlesAndSelf=e.getBundlesInDependencyOrder();
		Collections.sort(bundlesAndSelf, new BinFirstSorter());
		List<BundleManifest> bundles=new ArrayList<>(bundlesAndSelf);
		bundles.remove(src);

		File tmpDir=new File(bgc.out, l.jarName+".temp");
		String target=l.jarName+".jar";
		addTarget(target);
		writeObject(target);
		write(": ");
		writeObject(getBuildTargetJar(src));
		write("\n\tmkdir -p \"");
		writeObject(tmpDir.getAbsolutePath());
		write("\"\n");
		for(BundleManifest dep: bundlesAndSelf)
		{
			switch (dep.type) {
			case binary:
				for(String classPathFolder: getClassPathFolders(dep, dep==src))
				{
					File p=new File(classPathFolder);
					if(p.isDirectory())
					{
						write("\tcp -r ");
						writeObject(p.getAbsolutePath()+"/*");
						write(" \"");
						writeObject(tmpDir.getAbsolutePath());
						write("\"\n");
					}else if(p.isFile() && p.getName().endsWith(".jar"))
					{
						write("\tcd \"");
						writeObject(tmpDir.getAbsolutePath());
						write("\";unzip -o ");
						writeObject(p.getAbsolutePath());
						write("\n");
					}
				}
				break;
			case dummy:
				break;
			case source:
			write("\tcd \"");
			writeObject(tmpDir.getAbsolutePath());
			write("\";unzip -o ../");
			writeObject(getBuildTargetJar(dep));
			write("\n");
				break;
			default:
				throw new RuntimeException("Type not known");
			}
		}
		new ManifestTemplate(bgc, l.mainClass, l.jarName+".MANIFEST.MF", false).generate();
		write("\trm -rf \"");
		writeObject(tmpDir.getAbsolutePath());
		write("/META-INF/\"\n\tmkdir -p \"");
		writeObject(tmpDir.getAbsolutePath());
		write("/META-INF/\"\n\tcp ");
		writeObject(l.jarName+".MANIFEST.MF");
		write(" \"");
		writeObject(tmpDir.getAbsolutePath());
		write("/META-INF/MANIFEST.MF\"\n\trm -f ");
		writeObject(target);
		write("\n\tcd \"");
		writeObject(tmpDir.getAbsolutePath());
		write("\";zip -r ../");
		writeObject(target);
		write(" .\n\n\n");
	}
}
