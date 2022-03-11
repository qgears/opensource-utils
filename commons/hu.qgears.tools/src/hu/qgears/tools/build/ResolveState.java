package hu.qgears.tools.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

public class ResolveState {
	public class Checkpoint
	{
		private Checkpoint sub=null;
		public Checkpoint parent;
		private boolean thisError;
		private List<Consumer<Checkpoint>> undos=new ArrayList<>();
		/**
		 * Check if this dependency is already resolved by the current result set
		 * @param depC
		 * @return
		 */
		public boolean alreadyResolved(DependencyContainer depC) {
			return ResolveState.alreadyResolved(resultBundles, depC);
//			List<BundleManifest> possibles=resultBundles.getByOfferId(depC.dep);
//			if(possibles!=null)
//			{
//				for(BundleManifest m: possibles)
//				{
//					if(m.offers(depC.dep))
//					{
//						// Dependency is already resolved.
//						return true;
//					}
//				}
//			}
//			return false;
		}
		public DependencyContainer removeDepToResolve() {
			if(depsToResolve.size()==0)
			{
				return null;
			}
			String key=depsToResolve.keySet().iterator().next();
			DependencyContainer ret=depsToResolve.remove(key);
			depsResolved.put(key, ret);
			undos.add(x->depsToResolve.put(key, ret));
			undos.add(x->depsResolved.remove(key));
			return ret;
		}
		public void error(String string) {
			if(errors.add(string))
			{
				thisError=true;
				undos.add(x->errors.remove(string));
			}
		}
		public boolean isVersionCollision(BundleManifest b)
		{
			Set<BundleManifest> alreadyInWithId = resultBundles.getById(b.id);
			return alreadyInWithId.size() == 1 && alreadyInWithId.iterator().next() != b;
		}
		public void addBundle(BundleManifest b) {
			if(isVersionCollision(b))
			{
				Set<BundleManifest> alreadyInWithId = resultBundles.getById(b.id);
				error("Multiple Bundles with same id but different version! " + alreadyInWithId + " " + b);
			}
			resultBundles.add(b);
			addDeps(b, b.deps);
			undos.add(x->{
				resultBundles.remove(b);
			});
		}
		private void addDeps(BundleManifest bm, List<AbstractDependency> deps2) {
			for (AbstractDependency dep : deps2) {
				if(dep.optional&&bm.type==EBundleType.binary)
				{
					continue;
				}
				String key=dep.toString();
				DependencyContainer resolved=depsResolved.get(key);
				if(resolved!=null)
				{
					resolved.addOwner(bm);
					undos.add(x->resolved.removeOwner(bm));
				}else
				{
					DependencyContainer dc = depsToResolve.get(key);
					if (dc == null) {
						dc = new DependencyContainer(dep);
						depsToResolve.put(key, dc);
						undos.add(x->depsToResolve.remove(key));
					}
					dc.addOwner(bm);
					DependencyContainer dc2=dc;
					undos.add(x->dc2.removeOwner(bm));
				}
			}
		}
		public boolean isBranchError() {
			if(thisError)
			{
				return true;
			}
			if(sub!=null)
			{
				return sub.isBranchError();
			}
			return false;
		}
		public Checkpoint checkpoint() {
			return ResolveState.this.checkpoint();
		}
		public Checkpoint revert() {
			ResolveState.this.revertTo(this);
			return parent;
		}
		public void revertInternal() {
			for(int i=undos.size()-1; i>=0; --i)
			{
				Consumer<ResolveState.Checkpoint> u=undos.get(i);
				u.accept(this);
			}
		}
		public boolean hasBackTrack() {
			return parent!=null;
		}
		public boolean hasDepToResolve() {
			return depsToResolve.size()>0;
		}
	}
	public BundleSet resultBundles;
	/** We user ordered map so hopefully the dependencies which have constraints on each other are done close to each other */
	Map<String, DependencyContainer> depsToResolve = new TreeMap<>();
	Map<String, DependencyContainer> depsResolved = new TreeMap<>();
	private Checkpoint current=null;
	private TreeSet<String> errors=new TreeSet<>();
	
	public Checkpoint checkpoint() {
		Checkpoint ret=new Checkpoint();
		ret.parent=current;
		if(current!=null)
		{
			current.sub=ret;
		}
		current=ret;
		return ret;
	}

	@SuppressWarnings("unused")
	private boolean isError() {
		return errors.size()>0;
	}

	private void revertTo(Checkpoint c) {
		if(c.sub!=null)
		{
			revertTo(c.sub);
		}
		if(c!=current)
		{
			throw new RuntimeException("Internal error");
		}
		c.revertInternal();
		// undo all tasks in checkpoint
		current=c.parent;
		current.sub=null;
	}

	public List<String> getErrors() {
		return new ArrayList<>(errors);
	}

	public List<DependencyContainer> getDepsResolved() {
		return new ArrayList<>(depsResolved.values());
	}
	public static boolean alreadyResolved(BundleSet resultBundles, DependencyContainer depC) {
		List<BundleManifest> possibles=resultBundles.getByOfferId(depC.dep);
		if(possibles!=null)
		{
			for(BundleManifest m: possibles)
			{
				if(m.offers(depC.dep))
				{
					// Dependency is already resolved.
					return true;
				}
			}
		}
		return false;
	}

}
