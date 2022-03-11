package hu.qgears.tools.build.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class CompileCommandBuilder extends CompilerVersionConfiguration {
	/**
	 * Logger for compiler events.
	 * If null then diagnostics are written to stderr.
	 */
	public ICompileCallback compilerCallback;
	public TreeSet<String> binaryDeps=new TreeSet<>();
	public TreeSet<String> sourceDeps=new TreeSet<>();
	/**
	 * Classes to compile - absolute path.
	 */
	public TreeSet<String> classesToCompile=new TreeSet<>();
	/**
	 * Folder in which the make process is executed. Output is relative to this folder.
	 */
	public File makeFolder;
	/**
	 * Relative path to the container of the make file
	 */
	public String outputRelativePath;
	public File createGetOutputFolder() {
		if(makeFolder==null)
		{
			throw new NullPointerException();
		}
		File out=new File(makeFolder, outputRelativePath);
		out.mkdirs();
		return out;
	}
	private StandardJavaFileManager standardFileManager;
	public JavaFileManager wrapFileManager(StandardJavaFileManager standardFileManager) {
		this.standardFileManager=standardFileManager;
		return standardFileManager;
	}
	public Iterable<? extends JavaFileObject> getCompilationUnits() {
		List<File> filesToCompile=new ArrayList<>();
		for(String s: classesToCompile)
		{
			filesToCompile.add(new File(s));
		}

		return standardFileManager.getJavaFileObjectsFromFiles(filesToCompile);
	}
	final public void processDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
		try {
			if(compilerCallback!=null)
			{
				compilerCallback.processDiagnostics(diagnostics);
			}else
			{
				for(Diagnostic<? extends JavaFileObject> d: diagnostics.getDiagnostics())
				{
					System.err.println(""+d);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
