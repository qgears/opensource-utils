package hu.qgears.tools.build;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.tools.build.gen.FileContent;

public class NullBuildExtensions implements IBuildExtensions {
	class Project implements IBuildExtensionForProject
	{
		@Override
		public List<FileContent> additionalFiles() {
			return new ArrayList<>();
		}
	}
	@Override
	public IBuildExtensionForProject createProject(LauncherData l) {
		return new Project();
	}

}
