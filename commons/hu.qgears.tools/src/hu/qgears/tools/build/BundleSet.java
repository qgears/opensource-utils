package hu.qgears.tools.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.commons.MultiMapHashToHashSetImpl;
import hu.qgears.commons.Pair;

/**
 * A set of bundles. Basically a list but caching of queries is possible.
 * @author rizsi
 *
 */
public class BundleSet {
	List<BundleManifest> all=new ArrayList<>();
	MultiMapHashImpl<Pair<EDepType, String>, BundleManifest> m=new MultiMapHashImpl<>();
	MultiMapHashImpl<String, BundleManifest> fragmentQueries=new MultiMapHashImpl<>();
	MultiMapHashToHashSetImpl<String, BundleManifest> byId=new MultiMapHashToHashSetImpl<>();
	public void add(BundleManifest bmf) {
		all.add(bmf);
		for(AbstractOffer o: bmf.offers)
		{
			m.putSingle(o.getKey(), bmf);
		}
		if(bmf.fragmentHost!=null)
		{
			fragmentQueries.putSingle(bmf.fragmentHost.id, bmf);
		}
		byId.putSingle(bmf.id, bmf);
	}
	public void remove(BundleManifest bmf) {
		all.remove(bmf);
		for(AbstractOffer o: bmf.offers)
		{
			m.removeSingle(o.getKey(), bmf);
		}
		if(bmf.fragmentHost!=null)
		{
			fragmentQueries.removeSingle(bmf.fragmentHost.id, bmf);
		}
		byId.removeSingle(bmf.id, bmf);
	}

	/**
	 * A copy of the internal list.
	 * @return
	 */
	public List<BundleManifest> getAll() {
		return new ArrayList<>(all);
	}

	public List<BundleManifest> getByOfferId(AbstractDependency dep) {
		Pair<EDepType, String> id=new Pair<EDepType, String>(dep.getType(), dep.id);
		return m.getPossibleNull(id);
	}

	public List<BundleManifest> getFragmentQueriesById(String bundleId) {
		return fragmentQueries.get(bundleId);
	}

	public BundleSet cloneSet() {
		BundleSet ret=new BundleSet();
		for(BundleManifest b: all)
		{
			ret.add(b);
		}
		return ret;
	}
	public Set<BundleManifest> getById(String id) {
		return byId.get(id);
	}
}
