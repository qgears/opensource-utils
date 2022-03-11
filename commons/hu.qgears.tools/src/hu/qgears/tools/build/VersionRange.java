package hu.qgears.tools.build;

public class VersionRange {

	public Version min;
	public Version max;
	public boolean minExcluding;
	public boolean maxExcluding;
	@Override
	public String toString() {
		StringBuilder ret=new StringBuilder();
		if(minExcluding)
		{
			ret.append("(");
		}else
		{
			ret.append("[");
		}
		ret.append(min);
		ret.append(",");
		ret.append(max);
		if(maxExcluding)
		{
			ret.append(")");
		}else
		{
			ret.append("]");
		}
		return ret.toString();
	}
}
