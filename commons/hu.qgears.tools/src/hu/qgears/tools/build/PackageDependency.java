package hu.qgears.tools.build;

public class PackageDependency extends AbstractDependency
{

	public PackageDependency(String id) {
		super(id);
	}

	@Override
	public EDepType getType() {
		return EDepType.depPackage;
	}

}
