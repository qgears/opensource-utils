package hu.qgears.tools.build;

import java.util.List;

import hu.qgears.tools.build.gen.FileContent;

public interface IBuildExtensionForProject {

	List<FileContent> additionalFiles();

}
