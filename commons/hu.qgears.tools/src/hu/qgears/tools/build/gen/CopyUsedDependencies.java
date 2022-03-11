package hu.qgears.tools.build.gen;

import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.EBundleType;

public class CopyUsedDependencies extends RAbstractTemplatePart
{
	BuildGenContext bgc;
	public CopyUsedDependencies(BuildGenContext codeGeneratorContext) {
		super(codeGeneratorContext);
		bgc=codeGeneratorContext;
	}
	public void generate() {
		write("#!/bin/sh\n# Copy all of the binary dependencies used to a different folder\n# Can be used to create a new - minimal pool of used bundles for later builds and reproducibility\n");
		for(BundleManifest m: bgc.r.getBundlesInDependencyOrder())
		{
			if(m.type==EBundleType.binary)
			{
				write("cp -r \"");
				writeObject(m.projectFile.getAbsolutePath());
				write("\" $1\n");
			}
		}
		write("\n");
		finishCodeGeneration("copyUsedDependencies.sh");
	}

}
