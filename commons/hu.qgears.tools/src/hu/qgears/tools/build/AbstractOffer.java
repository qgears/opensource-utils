package hu.qgears.tools.build;

import hu.qgears.commons.Pair;

abstract public class AbstractOffer {
	public String id;
	public Version version=new Version();
	public AbstractOffer(String id, Version version) {
		super();
		this.id = id;
		this.version = version;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName()+":"+id+":"+version;
	}
	abstract public EDepType getType();
	public Pair<EDepType, String> getKey() {
		return new Pair<EDepType, String>(getType(), id);
	}
}
