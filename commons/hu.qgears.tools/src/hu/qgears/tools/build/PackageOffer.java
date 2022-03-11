package hu.qgears.tools.build;

public class PackageOffer extends AbstractOffer
{
	public PackageOffer(String id, Version version) {
		super(id, version);
	}

	@Override
	public EDepType getType() {
		return EDepType.depPackage;
	}
}
