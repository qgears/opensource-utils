package hu.qgears.tools.build.compiler;

import java.util.List;

/**
 * Configure source and target Java version compatibility of the compiling process.
 */
public class CompilerVersionConfiguration {
	/** Compatibility version of the source code.
	 *  null means unconfigured default for the compiler.
	 *  Example: "1.8" */
	public String source=null;
	/** Target class file version.
	 *  null means unconfigured default of the compiler that is the same as source version. */
	public String target=null;
	public void copyCompilerVersionFrom(CompilerVersionConfiguration config) {
		source=config.source;
		target=config.target;
	}
	/**
	 * When building option list for the compiler add the version specification
	 * to the command line arguments.
	 * @param optionList
	 */
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
	/**
	 * Change the source field on this object.
	 * @param string
	 * @return this same object after modifying its state.
	 */
	public CompilerVersionConfiguration setSource(String string) {
		source=string;
		return this;
	}
	/**
	 * Change the target field on this object.
	 * @param string
	 * @return this same object after modifying its state.
	 */
	public CompilerVersionConfiguration setTarget(String string) {
		target=string;
		return this;
	}
}
