package hu.qgears.tools.build;

abstract public class AbstractDependency {
	String id;
	public boolean optional;
	public boolean reexport;
	public VersionRange versionRange=new VersionRange();

	public AbstractDependency(String id) {
		super();
		this.id = id;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+":"+id+":"+versionRange+(optional?":optional":"")+(reexport?"reexport":"");
	}

	public boolean isOfferedBy(AbstractOffer o) {
		if(o.getType()==getType())
		{
			if(o.id.equals(id))
			{
				if(o.version.matches(versionRange))
				{
					return true;
				}
			}
		}
		return false;
	}

	abstract public EDepType getType();
}
