package hu.qgears.tools.build.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hu.qgears.commons.UtilFile;
import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.TemplateTracker;
import hu.qgears.tools.build.Args;
import hu.qgears.tools.build.Resolver;

public class BuildGenContext implements ICodeGeneratorContext, AutoCloseable {
	File out;
	public Resolver r;
	public Args args;
	private Writer duplicateWriter;
	public BuildGenContext(File out, Args args) throws IOException {
		super();
		this.out = out;
		this.args=args;
		duplicateWriter = new OutputStreamWriter(new FileOutputStream(new File(out, "duplicateFiles.txt")), StandardCharsets.UTF_8);
	}

	@Override
	public boolean needReport() {
		return false;
	}

	@Override
	public void createFile(String path, String o) {
		File f=new File(out, path);
		f.getParentFile().mkdirs();
		try {
			UtilFile.saveAsFile(f, o);
		} catch (IOException e) {
			error(e);
		}
	}

	private void error(IOException e) {
		e.printStackTrace();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createReport(String path, String o, TemplateTracker tt) {
	}

	public void duplicateFiles(List<FileContent> fcs) throws IOException {
		duplicateWriter.write("Duplicate files: "+fcs);
		duplicateWriter.write("\n");
	}

	@Override
	public void close() throws Exception {
		duplicateWriter.close();
	}
}
