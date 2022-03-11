package hu.qgears.tools.build;

import java.util.Comparator;

public class BinFirstSorter implements Comparator<BundleManifest> {

	@Override
	public int compare(BundleManifest o1, BundleManifest o2) {
		int ret=Integer.compare(o1.type.ordinal(), o2.type.ordinal());
		return ret;
	}

}
