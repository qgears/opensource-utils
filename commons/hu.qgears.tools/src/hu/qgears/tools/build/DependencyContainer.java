package hu.qgears.tools.build;

import java.util.HashSet;
import java.util.Set;

public class DependencyContainer
{
	AbstractDependency dep;
	Set<BundleManifest> owners=new HashSet<>();

	public DependencyContainer(AbstractDependency dep) {
		super();
		this.dep = dep;
	}

	public void addOwner(BundleManifest bm) {
		owners.add(bm);
	}
	@Override
	public String toString() {
		return "Dep: "+dep+" by "+owners;
	}

	public void removeOwner(BundleManifest bm) {
		owners.remove(bm);
	}
}
