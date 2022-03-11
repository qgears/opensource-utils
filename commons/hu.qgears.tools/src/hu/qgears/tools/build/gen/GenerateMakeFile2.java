package hu.qgears.tools.build.gen;

import java.util.List;

import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.Resolver;
import hu.qgears.tools.build.compiler.CompileCommandBuilder;

/**
 * Build all source files of the project in a single run.
 * TODO Then sort the generated classes into the target launchers.
 */
public class GenerateMakeFile2 extends AbstractMakeFileGenerator
{
	protected BuildGenContext bgc;
	public GenerateMakeFile2(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
		this.bgc=codeGeneratorContext;
	}
	@Override
	protected void generateTargets() {
		for(BundleManifest src: bgc.r.allTobuildBundles.getAll())
		{
			processSingle(src);
		}
		addTarget("alluber");
		compileCommand.outputRelativePath="out";
		write("\nallUber:\n\tmkdir -p out\n");
		generateCompileCommand(compileCommand);
	}
	CompileCommandBuilder compileCommand=new CompileCommandBuilder();
	private void processSingle(BundleManifest src) {
		BundleSet bs=new BundleSet();
		BundleSet resultBundles=new BundleSet();
		bs.add(src);
		Resolver e=new Resolver(bs, bgc.r.getResultBundlesAsBundleSet(), resultBundles);
		e.resolve();
		List<BundleManifest> bundles=resultBundles.getAll();
		bundles.remove(src);
		List<BundleManifest> bundlesAndSelf=resultBundles.getAll();
		for(BundleManifest dep:bundlesAndSelf)
		{
			switch(dep.type)
			{
			case binary:
				compileCommand.binaryDeps.addAll(getClassPathFolders(dep, dep==src));
				break;
			case source:
				compileCommand.sourceDeps.addAll(getClassPathFolders(dep, dep==src));
				break;
			case dummy:
				break;
			default:
				throw new RuntimeException();
			}
		}
		compileCommand.classesToCompile.addAll(findClassNames(src));
	}
}
