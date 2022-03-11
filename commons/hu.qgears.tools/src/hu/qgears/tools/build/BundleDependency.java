package hu.qgears.tools.build;

public class BundleDependency extends AbstractDependency
{
	public BundleDependency(String id) {
		super(id);
	}

	@Override
	public EDepType getType() {
		return EDepType.depBundle;
	}
}
