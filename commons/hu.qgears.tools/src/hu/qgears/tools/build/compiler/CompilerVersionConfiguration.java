package hu.qgears.tools.build.compiler;

import java.util.List;

public class CompilerVersionConfiguration {
	/** Compatibility version of the source code. */
	public String source=null; // Example: "1.8"
	/** Target class file version */
	public String target=null; // default is same as source "1.8"
	public void copyCompilerVersionFrom(CompilerVersionConfiguration config) {
		source=config.source;
		target=config.target;
	}
	public void configureSourceAndTargetVersions(List<String> optionList) {
		if(source!=null)
		{
			optionList.add("-source");
			optionList.add(source);
		}
		if(target!=null)
		{
			optionList.add("-target");
			optionList.add(target);
		}
	}
	public CompilerVersionConfiguration setSource(String string) {
		source=string;
		return this;
	}
	public CompilerVersionConfiguration setTarget(String string) {
		target=string;
		return this;
	}
}
