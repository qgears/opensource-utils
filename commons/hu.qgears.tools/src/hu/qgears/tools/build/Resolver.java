package hu.qgears.tools.build;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.tools.build.ResolveState.Checkpoint;

public class Resolver {
	public Resolver(BundleSet allTobuildBundles, BundleSet allAvailableBundles, BundleSet resultBundles) {
		super();
		this.allTobuildBundles = allTobuildBundles;
		this.allAvailableBundles = allAvailableBundles;
		this.state.resultBundles = resultBundles;
	}
	public BundleSet allTobuildBundles;
	public BundleSet allAvailableBundles;
	public ResolveState state=new ResolveState();
	public ResolveState.Checkpoint checkpoint;
	/**
	 * 
	 * @return true if resolving the bundle was successful
	 */
	public void resolve() {
		checkpoint=state.checkpoint();
		for(BundleManifest b: allTobuildBundles.all)
		{
			checkpoint.addBundle(b);
		}
		while(checkpoint.hasDepToResolve())
		{
			resolveSingle();
		}
	}
	private void resolveSingle() {
		DependencyContainer depC=checkpoint.removeDepToResolve();
		if(depC!=null)
		{
			if(checkpoint.alreadyResolved(depC))
			{
				// nothing to do
				// depC=checkpoint.removeDepToResolve();
			}
			else
			{
				List<BundleManifest> possibles=findAllMatch(depC, 0);
				if(possibles==null||possibles.size()==0)
				{
					checkpoint.error("Dependency can not be resolved: "+depC);
				}else
				{
					for(int i=0; i<possibles.size(); ++i)
					{
						BundleManifest tryOne=possibles.get(i);
						Checkpoint previous=checkpoint;
						checkpoint=checkpoint.checkpoint();
						try
						{
							checkpoint.addBundle(tryOne);
							MultiMapHashImpl<String, BundleManifest> fragments=new MultiMapHashImpl<>();
							for(BundleManifest mf: findAllFragments(tryOne))
							{
								fragments.putSingle(mf.id, mf);
							}
							for(Collection<BundleManifest> l: fragments.values())
							{
								List<BundleManifest> ll=new ArrayList<>(l);
								Collections.sort(ll, new BestMatchSorter());
								checkpoint.addBundle(ll.get(0));
							}
							if(!checkpoint.isBranchError() || i==possibles.size()-1)
							{
								// In case we are in error state and we have more options then do not search on
								resolveSingle();
							}
							if(checkpoint.isBranchError())
							{
								if(i<possibles.size()-1)
								{
									// Backtrack - do not revert this turn but parent will revert it
//									System.out.println("BACKTRACK: "+possibles.get(i)+" to "+possibles.get(i+1)+" "+depC);
//									printErrors(System.out);
									checkpoint=checkpoint.revert();
								}else
								{
									if(checkpoint.hasBackTrack())
									{
										break;
									}else
									{
										printErrors(System.err);
										throw new RuntimeException("Can not resolve: "+depC);
									}
								}
							}else
							{
								if(i!=0)
								{
									// System.out.println("BACKTRACK selected: "+possibles.get(i));
								}
								break;
							}
						}finally
						{
							checkpoint=previous;
						}
					}
				}
			}
		}
	}
	private List<BundleManifest> findAllFragments(BundleManifest toAdd) {
		List<BundleManifest> ret=new ArrayList<>();
		List<BundleManifest> mans=allAvailableBundles.getFragmentQueriesById(toAdd.id);
		for(BundleManifest possible: mans)
		{
			if(possible.fragmentHost.isOfferedBy(toAdd.bundleOffer))
			{
				ret.add(possible);
			}
		}
		return ret;
	}
//	private void addBundle(BundleManifest toAdd) {
//		if(!resultBundles.all.contains(toAdd))
//		{
//			resultBundles.add(toAdd);
//			List<BundleManifest> fragments=findAllFragments(toAdd);
//			for(BundleManifest fragment: fragments)
//			{
//				resultBundles.add(fragment);
//				toProcess.add(fragment);
//			}
//		}
//	}
	/**
	 * 
	 * @param bm
	 * @param dep
	 * @param rec
	 * @return null means dependency is already offered by current set. List means the elements in the list offer the requirement.
	 */
	private List<BundleManifest> findAllMatch(DependencyContainer depC, int rec) {
		AbstractDependency dep=depC.dep;
		List<BundleManifest> possibleImpls=new ArrayList<>();
		List<BundleManifest> possibles=allAvailableBundles.getByOfferId(dep);
		if(possibles!=null)
		{
			for(BundleManifest m: possibles)
			{
				if(m.offers(dep))
				{
					//if(!checkpoint.isVersionCollision(m))
					{
						possibleImpls.add(m);
					}
				}
			}
		}
		if(possibleImpls.size()>1)
		{
			Collections.sort(possibleImpls, new BestMatchSorter());
		}
		if(possibleImpls.size()==0)
		{
			if(rec==0)
			{
				findAllMatch(depC, rec+1);
				checkpoint.error("Can not find implementation of dependency: "+depC);
			}
		}
		return possibleImpls;
	}
	public List<BundleManifest> getResultBundles() {
		return state.resultBundles.getAll();
	}
	public BundleSet getResultBundlesAsBundleSet() {
		return state.resultBundles;
	}
	public void printErrors(PrintStream ps) {
		for(String s: state.getErrors())
		{
			ps.println(s);
		}
	}
	public void printResolved() {
		System.out.println("All resolved dependencies:");
		for(DependencyContainer dc: state.getDepsResolved())
		{
			System.out.println(dc);
		}
	}
	public String getErrorsString() {
		StringBuilder ret=new StringBuilder();
		for(String s: state.getErrors())
		{
			ret.append(s);
			ret.append("\n");
		}
		return ret.toString();
	}
	public String getAllDependenciesAsString() {
		StringBuilder ret=new StringBuilder();
		for(DependencyContainer dc: state.getDepsResolved())
		{
			ret.append(dc.toString());
			ret.append("\n");
		}
		return ret.toString();
	}
	public List<BundleManifest> getBundlesInDependencyOrder()
	{
		List<BundleManifest> all=state.resultBundles.getAll();
		return all;
		// TODO proper impl
/*		BundleSet found=new BundleSet();
		while(all.size()>0)
		{
			int prevSize=all.size();
			List<BundleManifest> turn=new ArrayList<>();
			for(BundleManifest m: all)
			{
				boolean bundleOk=true;
				for(AbstractDependency d: m.deps)
				{
					DependencyContainer depC=new DependencyContainer(d);
					depC.addOwner(m);
					if(!ResolveState.alreadyResolved(found, depC))
					{
						bundleOk=false;
						break;
					}
				}
				if(bundleOk)
				{
					turn.add(m);
				}
			}
			for(BundleManifest m: turn)
			{
				found.add(m);
				all.remove(m);
			}
			if(all.size()==prevSize)
			{
				System.err.println("Not a DAG!"+all);
				for(BundleManifest m: all)
				{
					found.add(m);
					return found.all;
				}
			}
		}
		return found.all;
		*/
	}
}
