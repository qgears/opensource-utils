package hu.qgears.crossref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.commons.UtilEvent;

/**
 * A document is a compile unit, a single file that is rebuilt when a file has changed within the scope of the
 * incremental builder.
 */
public class Doc extends CrossRefObject {
	public final String id;
	private Set<Ref> refs=new HashSet<>();
	private Set<Obj> objs=new HashSet<>();
	/**
	 * Collect all changed that are reported after the transaction has finished.
	 */
	private Set<CrossRefObject> changed=new HashSet<>();
	public final UtilEvent<Set<CrossRefObject>> changesAtEndOfTransaction=new UtilEvent<>();
	public Doc(CrossRefManager crossRefManager, String identifier) {
		super(crossRefManager);
		id=identifier;
	}
	@Override
	public Doc getDoc() {
		return this;
	}
	public void addObject(Obj ret) {
		objs.add(ret);
		changed.add(ret);
		getHost().registerChangedDoc(this);
	}
	public List<Ref> getRefsSnapshot() {
		return new ArrayList<>(refs);
	}
	public List<Obj> getObjsSnapshot() {
		return new ArrayList<>(objs);
	}
	protected void addRef(Ref ref) {
		changed.add(ref);
		refs.add(ref);
		getHost().registerChangedDoc(this);
	}
	protected void removeObj(Obj ret) {
		objs.remove(ret);
		changed.add(ret);
		getHost().registerChangedDoc(this);
	}
	protected void removeRef(Ref r) {
		refs.remove(r);
		changed.add(r);
		getHost().registerChangedDoc(this);
	}
	public void refResolvedChanged(Ref ref) {
		changed.add(ref);
		getHost().registerChangedDoc(this);
	}
	public void notifyChanges(ICrossRefManagerListener[] ls) {
		Set<CrossRefObject> changes=changed;
		changed=new HashSet<>();
		for(ICrossRefManagerListener l: ls)
		{
			l.documentChanged(this, changes);
		}
		changesAtEndOfTransaction.eventHappened(changes);
	}
}
