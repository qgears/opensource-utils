package hu.qgears.tools.build.compiler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import hu.qgears.commons.UtilString;

public class ExecuteCompileCommand {
	public void exec(CompileCommandBuilder compileCommand) {
		JavaCompiler compiler=CompilerLoader.createCompilerInstance();
		File out = compileCommand.createGetOutputFolder();
		
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, Locale.US, StandardCharsets.UTF_8);
		JavaFileManager fileManager = compileCommand.wrapFileManager(standardFileManager);
		
		Iterable<? extends JavaFileObject> compilationUnits = compileCommand.getCompilationUnits();
		 
		List<String> optionList = new ArrayList<String>();
		// set compiler's classpath to be same as the runtime's
		List<String> allDeps=new ArrayList<>();
		if(allDeps.size()>0)
		{
			allDeps.addAll(compileCommand.binaryDeps);
			allDeps.addAll(compileCommand.sourceDeps);
			optionList.add("-classpath");
			optionList.add(UtilString.concat(allDeps, ":"));
		}else
		{
			// Default classpath is used
		}
		if(out!=null)
		{
			optionList.add("-d");
			optionList.add(out.getAbsolutePath());
		}
		compileCommand.configureSourceAndTargetVersions(optionList);
		CompilerLoader.workaroundECJIssue(compiler, standardFileManager);
		
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);
		Boolean ret=task.call();
		compileCommand.processDiagnostics(diagnostics);
		if(!ret)
		{
			throw new RuntimeException("Compile error");
		}
	}
}
