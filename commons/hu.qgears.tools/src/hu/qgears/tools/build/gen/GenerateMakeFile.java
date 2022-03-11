package hu.qgears.tools.build.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.qgears.commons.UtilComma;
import hu.qgears.tools.build.BinFirstSorter;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.LauncherData;
import hu.qgears.tools.build.Resolver;

public class GenerateMakeFile extends AbstractMakeFileGenerator{
	public GenerateMakeFile(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
	}
	@Override
	protected void generateTargets() {
		for(BundleManifest src: bgc.r.allTobuildBundles.getAll())
		{
			genCompile(src);
			for(LauncherData l: src.launchers)
			{
				genLauncher(src, l);
			}
		}
	}
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
		addTarget(target);
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
			write("\tcp -r ");
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
}
