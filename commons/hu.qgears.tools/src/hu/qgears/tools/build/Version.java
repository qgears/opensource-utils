package hu.qgears.tools.build;

import java.util.List;

import hu.qgears.commons.UtilString;

public class Version {
	public int a;
	public int b;
	public int c;
	public String specifier;
	@Override
	public String toString() {
		return ""+a+"."+b+"."+c+(specifier==null?"":"."+specifier);
	}
	
	public boolean matches(VersionRange r)
	{
		if(r.min!=null)
		{
			if(matches(r.min))
			{
				if(r.minExcluding)
				{
					return false;
				}
				if(r.min.specifier!=null && !r.min.specifier.equals(specifier))
				{
					return false;
				}
			}
			if(!isGreaterOrEqual(r.min))
			{
				return false;
			}
		}
		if(r.max!=null)
		{
			if(matches(r.max))
			{
				if(r.maxExcluding)
				{
					return false;
				}
				if(r.max.specifier!=null && !r.max.specifier.equals(specifier))
				{
					return false;
				}
			}
			if(!r.max.isGreaterOrEqual(this))
			{
				return false;
			}
		}
		return true;
	}

	public boolean isGreaterOrEqual(Version min) {
		if(a>min.a) return true;
		if(a<min.a) return false;
		if(b>min.b) return true;
		if(b<min.b) return false;
		if(c>min.c) return true;
		if(c<min.c) return false;
		return true;
	}

	/**
	 * Versions equal. In case the requirement does not specify a spec field it still matches.
	 * @param requirement
	 * @return
	 */
	public boolean matches(Version requirement) {
		return (requirement.a==a && requirement.b==b && requirement.c==c
			&& (requirement.specifier==null || requirement.specifier.equals(specifier))	
				);
	}

	public int compareTo(Version version) {
		int ret=Integer.compare(a, version.a);
		if(ret!=0)
		{
			return ret;
		}
		ret=Integer.compare(b, version.b);
		if(ret!=0)
		{
			return ret;
		}
		ret=Integer.compare(c, version.c);
		if(ret!=0)
		{
			return ret;
		}
		if(specifier==null)
		{
			return 1;
		}
		if(version.specifier==null)
		{
			return -1;
		}
		return specifier.compareTo(version.specifier);
	}

	public Version parse(String v) {
		a=b=c=0;
		specifier=null;
		List<String> pieces=UtilString.split(v, ".");
		if(pieces.size()>0)
		{
			a=Integer.parseInt(pieces.get(0));
		}
		if(pieces.size()>1)
		{
			b=Integer.parseInt(pieces.get(1));
		}
		if(pieces.size()>2)
		{
			c=Integer.parseInt(pieces.get(2));
		}
		if(pieces.size()>3)
		{
			specifier=pieces.get(3);
		}
		if(pieces.size()>4)
		{
			throw new IllegalArgumentException();
		}
		return this;
	}

}
