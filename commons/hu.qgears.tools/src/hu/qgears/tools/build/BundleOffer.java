package hu.qgears.tools.build;

public class BundleOffer extends AbstractOffer
{
	public BundleOffer(String id, Version version) {
		super(id, version);
	}

	@Override
	public EDepType getType() {
		return EDepType.depBundle;
	}
}
