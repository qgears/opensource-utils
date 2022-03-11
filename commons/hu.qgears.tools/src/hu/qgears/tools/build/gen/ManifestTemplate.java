package hu.qgears.tools.build.gen;

import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class ManifestTemplate extends RAbstractTemplatePart
{
	private String mainClass;
	private String path;
	private boolean agent;
	public ManifestTemplate(ICodeGeneratorContext codeGeneratorContext, String mainClass, String path, boolean agent) {
		super(codeGeneratorContext);
		this.mainClass=mainClass;
		this.path=path;
		this.agent=agent;
	}
	public ManifestTemplate(RAbstractTemplatePart parent, String mainClass, String path, boolean agent) {
		super(parent);
		this.mainClass=mainClass;
		this.path=path;
		this.agent=agent;
	}
	public void generateContent()
	{
		write("Manifest-Version: 1.0\nClass-Path: .\n");
		if(agent)
		{
			write("Agent-Class: ");
			writeObject(mainClass);
			write("\n");
		} else {
			write("Main-Class: ");
			writeObject(mainClass);
			write("\n");
		}
		write("\n\n");
	}
	public void generate()
	{
		generateContent();
		finishCodeGeneration(path);
	}
}
