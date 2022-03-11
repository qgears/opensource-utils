package hu.qgears.tools.build;

import java.util.Comparator;

public class BestMatchSorter implements Comparator<BundleManifest> {

	@SuppressWarnings("unused")
	private int compareReverse(BundleManifest o1, BundleManifest o2) {
		// TODO sorting fails
		int ret=Integer.compare(o2.type.ordinal(), o1.type.ordinal());
		if(ret!=0)
		{
			return ret;
		}
		ret=o2.id.compareTo(o1.id);
		if(ret!=0)
		{
			return ret;
		}
		return o2.version.compareTo(o1.version);
	}
	@Override
	public int compare(BundleManifest o1, BundleManifest o2) {
		int ret=Integer.compare(o2.type.ordinal(), o1.type.ordinal());
		if(ret!=0)
		{
			return ret;
		}
		ret=o2.id.compareTo(o1.id);
		if(ret!=0)
		{
			return ret;
		}
		return o2.version.compareTo(o1.version);
	}

}
