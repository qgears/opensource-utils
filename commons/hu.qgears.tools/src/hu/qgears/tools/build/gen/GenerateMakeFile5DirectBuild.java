package hu.qgears.tools.build.gen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import hu.qgears.commons.MultiMapTreeImpl;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilTime;
import hu.qgears.rtemplate.runtime.RQuickTemplate;
import hu.qgears.tools.build.BinFirstSorter;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.IBuildExtensionForProject;
import hu.qgears.tools.build.IFileOutput;
import hu.qgears.tools.build.LauncherData;
import hu.qgears.tools.build.Resolver;
import hu.qgears.tools.build.ZipFileOutput;
import hu.qgears.tools.build.compiler.CompileCommandBuilder;
import hu.qgears.tools.build.compiler.ExecuteCompileCommand;
import hu.qgears.tools.build.compiler.ICompileCallback;

/**
 * Build the targets within the JVM using the JavaCompiler framework.
 * @author rizsi
 *
 */
public class GenerateMakeFile5DirectBuild extends AbstractMakeFileGenerator{
	String[] omitPrefixes=new String[]{"META-INF/", "about.html", ".options", ".api_description", 
			"plugin.properties", "about_files/",
			"plugin.xml",
			"OSGI-INF",
			"about.ini",
			"about.mappings",
			"about.properties",
			"fragment.properties",
			"icons/full/", // eclipse ui ide blabla
			"modeling32.png"
			};

	public GenerateMakeFile5DirectBuild(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
	}
	@Override
	protected void generateTargets() throws Exception {
		UtilTime t=UtilTime.createTimer();
		for(BundleManifest src: bgc.r.allTobuildBundles.getAll())
		{
			if(src.launchers.size()>0||src.agents.size()>0)
			{
				new GenLauncher().genLaunchers(src, src.launchers, src.agents);
			}
		}
		t.printElapsed("Compile all");
	}

	private class GenLauncher
	{
		MultiMapTreeImpl<String, FileContent> filesInPackage=new MultiMapTreeImpl<>();
		CompileCommandBuilder compileCommand=new CompileCommandBuilder();
		public void genLaunchers(BundleManifest src, List<LauncherData> launchers, List<LauncherData> agents) throws Exception {
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
			compileCommand.makeFolder=bgc.out;
			compileCommand.compilerCallback=new ICompileCallback() {
				@Override
				public void processDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
					try {
						try(FileOutputStream fos=new FileOutputStream(new File(bgc.out, "compilerDiagnostics.txt")))
						{
							try(OutputStreamWriter osw=new OutputStreamWriter(fos, StandardCharsets.UTF_8))
							{
								for(Diagnostic<? extends JavaFileObject> d: diagnostics.getDiagnostics())
								{
									osw.write(""+d+"\n");
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new ExecuteCompileCommand().exec(compileCommand);
			filesInPackage=new MultiMapTreeImpl<>();
			for(BundleManifest dep: bundlesAndSelf)
			{
				switch (dep.type) {
				case binary:
					for(String classPathFolder: getClassPathFolders(dep, dep==src))
					{
						File p=new File(classPathFolder);
						if(p.isDirectory())
						{
							new UtilFileVisitor(){
								@Override
								protected boolean visited(File dir, String localPath, int depth) throws Exception {
									if(dir.isFile())
									{
										addFile(new FileContent(localPath, dir));
									}
									return super.visited(dir, localPath, depth);
								}
							}.visit(p);
						}else if(p.isFile() && p.getName().endsWith(".jar"))
						{
							Map<String, FileContent> fcs=new TreeMap<>();
							try(FileInputStream fis=new FileInputStream(p))
							{
								try(ZipInputStream zis=new ZipInputStream(fis))
								{
									ZipEntry ze;
									while((ze = zis.getNextEntry())!=null)
									{
										ByteArrayOutputStream bos = new ByteArrayOutputStream();
										byte[] buffer = new byte[UtilFile.defaultBufferSize.get()];
										int n;
										while ((n = zis.read(buffer)) > 0) {
											bos.write(buffer, 0, n);
										}
										if(!ze.isDirectory())
										{
											FileContent fc=new FileContent(ze.getName(), p, bos);
											FileContent prev=fcs.put(fc.id, fc);
											if(prev!=null)
											{
												System.out.println("Multiple entries in single zip: "+prev);
											}
										}
									}
								}
							}
							for(FileContent fc: fcs.values())
							{
								addFile(fc);
							}
						}
					}
					break;
				case dummy:
					break;
				case source:
					List<String> sourcefolders=getSourceFolders(dep);
					for(String sf: sourcefolders)
					{
						File srcFolder=new File(sf);
						addClassesAndResourcesFromFolder(srcFolder);
					}
					for(String path: dep.cph.libFolders)
					{
						addClassesAndResourcesFromFolder(new File(dep.projectFile, path));
					}
					break;
				default:
					throw new RuntimeException("Type not known");
				}
			}
			addClassesAndResourcesFromFolder(new File(bgc.out, compileCommand.outputRelativePath));
			for(String id: filesInPackage.keySet())
			{
				List<FileContent> fcs=filesInPackage.get(id);
				if(fcs.size()>1)
				{
					bgc.duplicateFiles(fcs);
				}
			}
			for(LauncherData l: launchers)
			{
				generateJarFile(l, false);
			}
			for(LauncherData l: agents)
			{
				generateJarFile(l, true);
			}
		}
		private void generateJarFile(LauncherData l, boolean agent) throws FileNotFoundException, IOException {
			IBuildExtensionForProject bep=bgc.args.buildExtensions.createProject(l);
			List<FileContent> output=new ArrayList<>();
			String mfcontent=new RQuickTemplate() {
				@Override
				protected void doGenerate() {
					new ManifestTemplate(this, l.mainClass, "", agent).generateContent();
				}
			}.generate();
			FileContent mf=new FileContent("META-INF/MANIFEST.MF", mfcontent.getBytes(StandardCharsets.UTF_8));
			output.add(mf);
			MultiMapTreeImpl<String, FileContent> filesInPackageCopy=deepCopy(filesInPackage);
			for(FileContent fc: bep.additionalFiles())
			{
				filesInPackageCopy.putSingle(fc.id, fc);
			}
			for(Collection<FileContent> fcs: filesInPackageCopy.values())
			{
				List<FileContent> ll=new ArrayList<>(fcs);
				output.add(ll.get(ll.size()-1));
			}
			try(IFileOutput zfo=createOutputForJar(l.jarName))
			{
				for(FileContent fc: output)
				{
					try(OutputStream os=zfo.createOutputStream(fc.id, bgc.args.authorDate))
					{
						fc.streamConentTo(os);
					}
				}
			}
		}
		private IFileOutput createOutputForJar(String jarName) throws FileNotFoundException
		{
			IFileOutput out;
			out=bgc.args.outputMapper.get(jarName);
			if(out==null)
			{
				out=new ZipFileOutput(new ZipOutputStream(new FileOutputStream(new File(bgc.out, jarName+".jar"))));
			}
			out.open();
			return out;
		}
		public void addClassesAndResourcesFromFolder(File srcFolder) throws Exception {
			new UtilFileVisitor(){
				@Override
				protected boolean visited(File dir, String localPath, int depth) throws Exception {
					if(dir.isFile() && !localPath.endsWith(".java"))
					{
						addFile(new FileContent(localPath, dir));
					}
					return super.visited(dir, localPath, depth);
				}
			}.visit(srcFolder);
		}
		protected void addFile(FileContent fileContent) {
			for(String omitPrefix: omitPrefixes)
			{
				if(fileContent.id.startsWith(omitPrefix))
				{
					return;
				}
			}
//			if(filesInPackage.getPossibleNull(fileContent.id)!=null)
//			{
//				System.out.println("Entry clash: "+fileContent.id);
//			}
			filesInPackage.putSingle(fileContent.id, fileContent);
		}
	}

	public MultiMapTreeImpl<String, FileContent> deepCopy(MultiMapTreeImpl<String, FileContent> filesInPackage) {
		MultiMapTreeImpl<String, FileContent> ret=new MultiMapTreeImpl<>();
		for(String s: filesInPackage.keySet())
		{
			for(FileContent fc: filesInPackage.get(s))
			{
				ret.putSingle(fc.id, fc);
			}
		}
		return ret;
	}
}
