package hu.qgears.tools.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilString;
import hu.qgears.tools.build.gen.BuildGenContext;
import hu.qgears.tools.build.gen.CopyUsedDependencies;
import hu.qgears.tools.build.gen.GenerateMakeFile5DirectBuild;
import hu.qgears.tools.build.manifest.SimpleManifest;
import joptsimple.tool.AbstractTool;

public class BundleStandaloneBuild extends AbstractTool {
	public final static String MFPATH = "META-INF/MANIFEST.MF";

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}

	@Override
	public int doExec(IArgs ia) throws Exception {
		Args a = (Args) ia;
		final Set<String> exceptions = new HashSet<>();
		final Set<String> all = new HashSet<>();
		BundleSet allAvailableBundles = new BundleSet();
		BundleSet allTobuildBundles = new BundleSet();
		SAXParser saxParser=SAXParserFactory.newInstance().newSAXParser();
		addDummyBundles(allAvailableBundles);
		for (File d : a.sourceFolders) {
			if(!d.exists()||!d.isDirectory())
			{
				error("Missing source folder: "+d.getAbsolutePath());
			}
			new UtilFileVisitor() {
				@Override
				protected boolean visited(File dir, String localPath, final int depth) throws Exception {
					File cp = new File(dir, ".classpath");
					File mf = new File(dir, MFPATH);
					File pr = new File(dir, ".project");
					File launcher = new File(dir, "launcher.txt");
					// File launcher=new File(dir, args.launcherFile);
					if (pr.isFile() && mf.isFile() && cp.isFile()) {
						all.add(localPath);
						ClassPathHandler cph = new ClassPathHandler();
						saxParser.parse(cp, cph);
						SimpleManifest m = new SimpleManifest(new FileInputStream(mf));
						BundleManifest bmf = new BundleManifest();
						bmf.parse(m);
						bmf.projectFile=dir;
						bmf.cph=cph;
						if(!a.ignoreSourceProject.contains(bmf.id))
						{
							allAvailableBundles.add(bmf);
							allTobuildBundles.add(bmf);
						}
						bmf.launchers = LauncherData.parse(launcher);
						bmf.agents = LauncherData.parse(new File(dir, "agent.txt"));
						return false;
					} else {
						return !exceptions.contains(dir.getName());
					}
				}
			}.visit(d);
		}
		for (File pool : a.pools) {
			for (File f : UtilFile.listFiles(pool)) {
				try {
					BundleManifest bmf = null;
					if (f.isDirectory()) {
						File mf = new File(f, MFPATH);
						SimpleManifest m = new SimpleManifest(new FileInputStream(mf));
						bmf = new BundleManifest();
						bmf.parse(m);
					} else if (f.getName().endsWith(".jar")) {
						bmf = parseFromZipFile(f);
						bmf.type=EBundleType.binary;
					}
					if (bmf != null) {
						bmf.type=EBundleType.binary;
						bmf.projectFile=f;
						if(!a.ignoreBinaryProject.contains(bmf.id))
						{
							allAvailableBundles.add(bmf);
						}
					}
				} catch (Exception e) {
					System.err.println("Error parsing manifest of: " + f.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		BundleSet resultBundles=new BundleSet();
		Resolver r=new Resolver(allTobuildBundles, allAvailableBundles, resultBundles);
		
		a.out.mkdirs();
		UtilFile.saveAsFile(new File(a.out, "plugins-available.txt"), UtilString.concat(allAvailableBundles.all,new UtilComma("\n"),t->(t.toString()+" "+(t.projectFile==null?"NO FOLDER":t.projectFile.getAbsolutePath()))));
		
		UtilFile.saveAsFile(new File(a.out, "plugins-tobuild.txt"), UtilString.concat(allTobuildBundles.all,new UtilComma("\n"),t->(t.toString()+" "+(t.projectFile==null?"NO FOLDER":t.projectFile.getAbsolutePath()))));
		r.resolve();
		String s=r.getErrorsString();
		UtilFile.saveAsFile(new File(a.out, "errors.txt"), s);
		UtilFile.saveAsFile(new File(a.out, "all-dependencies.txt"), r.getAllDependenciesAsString());
		System.out.println("Resolving ready!");
		UtilFile.saveAsFile(new File(a.out, "runtimeplugins.txt"), UtilString.concat(r.getResultBundles(),new UtilComma("\n"),t->(t.toString()+" "+(t.projectFile==null?"NO FOLDER":t.projectFile.getAbsolutePath()))));
		try(BuildGenContext bgc=new BuildGenContext(a.out, a))
		{
			bgc.r=r;
			new CopyUsedDependencies(bgc).generate();
			new GenerateMakeFile5DirectBuild(bgc).generate();
		}
		return 0;
	}

	private void error(String string) {
		System.err.println(string);
	}

	private void addDummyBundles(BundleSet allAvailableBundles) {
		BundleManifest bm=BundleManifest.createDummy("system.bundle", "1.0.0");
		allAvailableBundles.add(bm);
	}

	private BundleManifest parseFromZipFile(File f) throws FileNotFoundException, IOException {
		try (InputStream input = new FileInputStream(f)) {
			try (ZipInputStream zip = new ZipInputStream(input)) {
				ZipEntry entry = zip.getNextEntry();
				while (entry != null) {
					if (entry.getName().equals(MFPATH)) {
						byte[] data=UtilFile.loadFile(zip);
						BundleManifest bmf = new BundleManifest();
						SimpleManifest m = new SimpleManifest(new ByteArrayInputStream(data));
						bmf.parse(m);
						return bmf;
					}
					entry=zip.getNextEntry();
				}
			}
		}
		throw new IllegalArgumentException(MFPATH + " not found");
	}

	@Override
	public String getDescription() {
		return "OSGI Bundle standalone build Make generator";
	}

	@Override
	public String getId() {
		return "bundle-standalone-build";
	}

	protected static void gitignore(File dir, String string) throws IOException {
		File f = new File(dir, ".gitignore");
		if (f.exists()) {
			String s = UtilFile.loadAsString(f);
			if (s.contains(string)) {
				return;
			}
			if (!s.endsWith("\n")) {
				s = s + "\n";
			}
			s = s + string + "\n";
			UtilFile.saveAsFile(f, s);
			return;
		}
		UtilFile.saveAsFile(f, string + "\n");
	}

	protected static String cropBefore(String string, String pattern) {
		int idx = string.indexOf(pattern);
		return idx < 0 ? string : string.substring(0, idx);
	}
}
