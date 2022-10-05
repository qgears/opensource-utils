package hu.qgears.crossref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import hu.qgears.commons.MultiMapHashToHashSetImpl;
import hu.qgears.commons.MultiMapTreeImpl;

/**
 * Cross reference manager for incremental builders.
 * See crossref.asciidoc
 * Must be accessed single threaded.
 */
public class CrossRefManager {
	private List<ICrossRefManagerListener> listeners=new ArrayList<>();
	/** Documents (source files) by their identifier. */
	private Map<String, Doc> docs=new TreeMap<>();
	// Objects stored to process when transaction was finished.
	private Set<Doc> toDelete=new HashSet<>();
	private Set<Ref> toResolve=new HashSet<>();
	private Set<Doc> changedDocs=new HashSet<>();
	private Object syncObj=new Object();
	/** Objects by their identifier. */
	private MultiMapTreeImpl<String, Obj> objects=new MultiMapTreeImpl<>();
	
	/** Objects by their local identifier. 
	 * It is normal that there are multiple objects with the same local ID. */
	private MultiMapHashToHashSetImpl<String, Obj> objectsByLocalId=new MultiMapHashToHashSetImpl<>();
	/** Objects by their type and local identifer concatenated. 
	 *  This is useful in cases when there are types that have global identifiers and we need a 
	 *  performing way to find them. */
	private MultiMapHashToHashSetImpl<String, Obj> objectsByTypeAndLocalId=new MultiMapHashToHashSetImpl<>();
	/** Objects by their type. */
	private MultiMapHashToHashSetImpl<String, Obj> objectsByType=new MultiMapHashToHashSetImpl<>();

	private MultiMapHashToHashSetImpl<String, GidSearch> refByTypeAndLocalId=new MultiMapHashToHashSetImpl<>();
	private MultiMapHashToHashSetImpl<String, GidSearch> refByFqId=new MultiMapHashToHashSetImpl<>();
	/**
	 * All references handled by this managed.
	 */
	private Set<Ref> refs=new HashSet<>();
	/**
	 * Create a new document with a given identifier.
	 * Within a single cross reference manager only one file may exist with a given name.
	 * Existing file with same id is disposed first. 
	 * @return
	 */
	public Doc createDocument(String identifier)
	{
		Doc prev=docs.remove(identifier);
		if(prev!=null)
		{
			prev.close();
		}
		Doc ret=new Doc(this, identifier);
		docs.put(identifier, ret);
		return ret;
	}
	public Obj createObj(Doc owner, String fqId, String type)
	{
		if(owner.isClosed())
		{
			throw new IllegalStateException();
		}
		Obj ret=new Obj(owner, fqId, type);
		synchronized (syncObj) {
			owner.addObject(ret);
			objects.putSingle(fqId, ret);
			objectsByLocalId.putSingle(ret.getLocalId(), ret);
			objectsByTypeAndLocalId.putSingle(ret.getTypeAndLocalId(), ret);
			objectsByType.putSingle(ret.getType(), ret);
			
			signalSearches(refByFqId.getPossibleNull(fqId), ret);
			signalSearches(refByTypeAndLocalId.getPossibleNull(fqId), ret);
		}
		return ret;
	}
	/**
	 * In case there are matching searches for this object
	 * then signal them to be updated at the end of the transaction.
	 * @param searches
	 * @param o
	 */
	private void signalSearches(HashSet<GidSearch> searches, Obj o) {
		if(searches!=null)
		{
			for(GidSearch gs: searches)
			{
				if(match(gs.ref, o))
				{
					toResolve.add(gs.ref);
				}
			}
		}
	}
	public static String getLocalId(String fqId) {
		int idx=fqId.lastIndexOf('.');
		if(idx>=0)
		{
			return fqId.substring(idx+1);
		}
		return fqId;
	}
	/**
	 * Finish pending deletes.
	 * 
	 * Resolve all references.
	 * 
	 * If there are new objects created during the close transaction phase then resolving is re-executed. 
	 */
	public void closeTransaction()
	{
		Set<Doc> ds=toDelete;
		toDelete=new HashSet<>();
		for(Doc d: ds)
		{
			for(Ref r: d.getRefsSnapshot())
			{
				r.close();
			}
			for(Obj o: d.getObjsSnapshot())
			{
				o.close();
			}
		}
		// While references are being resolved it is possible that the model changes (by listeners) that is why we have to repeat this cycle until all
		// References are processed.
		while(toResolve.size()>0)
		{
			Set<Ref> rs=toResolve;
			toResolve=new HashSet<>();
			for(Ref r: rs)
			{
				resolveReference(r);
			}
			for(ICrossRefManagerListener l: getListenersCopy())
			{
				l.resolveCycleFinished();
			}
		}
		List<Doc> ch=new ArrayList<>(changedDocs);
		changedDocs.clear();
		ICrossRefManagerListener[] ls=getListenersCopy();
		for(Doc d: ch)
		{
			d.notifyChanges(ls);
		}
		for(ICrossRefManagerListener l: ls)
		{
			l.transactionFinished();
		}
	}
//	private boolean resolve(Ref mr) {
//		// Try to resolve as local id
//		String globalId=mr.scope.createGlobalFromLocalId(mr.refId);
//		@SuppressWarnings("unchecked")
//		List<Obj> finds=objects.getPossibleDefault(globalId, Collections.EMPTY_LIST);
//		if(finds.size()>0)
//		{
//			mr.resolvedTo(finds);
//			return true;
//		}
//		// Try to resolve as global id
//		globalId=mr.refId;
//		finds=objects.getPossibleDefault(globalId, new ArrayList<>());
//		if(finds.size()>0)
//		{
//			mr.resolvedTo(finds);
//			return true;
//		}
//		return false;
//	}
	/**
	 * Create a reference to an other defined object.
	 * The reference is only resolved when closeTransaction() is called. For this reason it is safe to add listeners after creation.
	 * @param crossrefDoc Document owning this reference
	 * @param scope the search scope of this reference
	 * @param refId identifier of the reference
	 * @param refType type of the reference
	 * @param targetType
	 */
	public Ref createRef(Doc crossrefDoc, Scope scope) {
		if(crossrefDoc.isClosed())
		{
			throw new IllegalStateException();
		}
		Ref ref=new Ref(crossrefDoc, scope);
		synchronized (syncObj) {
			crossrefDoc.addRef(ref);
			toResolve.add(ref);
			refs.add(ref);
			scope.seal();
			List<String> pgid=scope.getPossibleGlobalIds();
			if(pgid!=null)
			{
				ref.scope.gidSearch=new ArrayList<>();
				for(int i=0;i<pgid.size();++i)
				{
					String gid=pgid.get(i);
					GidSearch gs=new GidSearch(gid, ref, i);
					refByFqId.putSingle(gs.gid, gs);
					ref.scope.gidSearch.add(gs);
				}
			}
			Set<String> allowedTypes=scope.getAllowedTypes();
			if(allowedTypes!=null && ref.scope.getLocalIdentifier()!=null)
			{
				ref.scope.typeSearch=new ArrayList<>();
				for(String type: allowedTypes)
				{
					String key=getTypeAndLocalId(type, ref.scope.getLocalIdentifier());
					GidSearch s=new GidSearch(key, ref, 0);
					refByTypeAndLocalId.putSingle(key, s);
					ref.scope.typeSearch.add(s);
				}
			}
			toResolve.add(ref);
		}
		return ref;
	}
	private void resolveReference(Ref ref) {
		if(ref.isClosed())
		{
			return;
		}
		Set<Obj> found=null;
		if(ref.scope.gidSearch!=null)
		{
			for(GidSearch gs: ref.scope.gidSearch)
			{
				List<Obj> matches=objects.getPossibleNull(gs.gid);
				if(matches!=null)
				{
					boolean f=false;
					for(Obj o: matches)
					{
						if(match(ref, o))
						{
							found=addFound(found, o);
							f=true;
						}
					}
					if(f)
					{
						break;
					}
				}
			}
		}
		if(ref.scope.typeSearch!=null)
		{
			for(GidSearch gs: ref.scope.typeSearch)
			{
				Set<Obj> matches=objectsByTypeAndLocalId.getPossibleNull(gs.gid);
				if(matches!=null&&matches.size()>0)
				{
					for(Obj o: matches)
					{
						if(match(ref, o))
						{
							found=addFound(found, o);
						}
					}
				}
			}
		}
		ref.setResolvedTo(found);
	}
	private Set<Obj> addFound(Set<Obj> found, Obj o) {
		if(found==null)
		{
			found=new HashSet<>();
		}
		found.add(o);
		return found;
	}
	/**
	 * Match the reference and object.
	 * Identifier match is already checked.
	 * Checks type and user defined matcher function if present
	 * @param ref
	 * @param o
	 * @return true means match is ok.
	 */
	private boolean match(Ref ref, Obj o)
	{
		if(ref.scope.getAllowedTypes()!=null)
		{
			if(!ref.scope.getAllowedTypes().contains(o.getType()))
			{
				return false;
			}
		}
		String fqId=ref.scope.getId();
		if(fqId!=null)
		{
			if(!fqId.equals(o.fqId))
			{
				return false;
			}
		}
		if(ref.scope.filterFunction!=null)
		{
			return ref.scope.filterFunction.isPossibleTarget(o);
		}
		return true;
	}
	public void closed(CrossRefObject crossRefObject) {
		if(crossRefObject instanceof Obj)
		{
			Obj ret=(Obj) crossRefObject;
			objects.removeSingle(ret.getFqId(), ret);
			objectsByLocalId.removeSingle(ret.getLocalId(), ret);
			objectsByTypeAndLocalId.removeSingle(ret.getTypeAndLocalId(), ret);
			objectsByType.removeSingle(ret.getType(), ret);
			ret.doc.removeObj(ret);
			for(Ref r: ret.referencesTargetingThis)
			{
				if(!r.isClosed())
				{
					toResolve.add(r);
				}
			}
		}
		else if(crossRefObject instanceof Ref)
		{
			Ref r=(Ref) crossRefObject;
			r.crossrefDoc.removeRef(r);
			if(r.scope.gidSearch!=null)
			{
				for(GidSearch gs: r.scope.gidSearch)
				{
					refByFqId.removeSingle(gs.gid, gs);
				}
			}
			if(r.scope.typeSearch!=null)
			{
				for(GidSearch gs: r.scope.typeSearch)
				{
					refByTypeAndLocalId.removeSingle(gs.gid, gs);
				}
			}
			if(r.resolvedTo!=null)
			{
				for(Obj o: r.resolvedTo)
				{
					o.referencesTargetingThis.remove(crossRefObject);
				}
			}
			toResolve.remove(r);
			refs.remove(r);
		} else if(crossRefObject instanceof Doc)
		{
			Doc d=(Doc) crossRefObject;
			toDelete.add(d);
			if(docs.get(d.id)==d)
			{
				docs.remove(d.id);
			}
		}
	}
	private ICrossRefManagerListener[] getListenersCopy()
	{
		return listeners.toArray(new ICrossRefManagerListener[] {});
	}
	public CrossRefManager addListener(ICrossRefManagerListener l)
	{
		listeners.add(l);
		return this;
	}
	/**
	 * Names of all objects known by this cross ref manager.
	 * @return
	 */
	public Set<String> getObjectNames() {
		return objects.keySet();
	}
	/**
	 * Find objects by their identifier.
	 * @param id
	 * @return all matches (size>0 means ambigous reference that is possible)
	 */
	public List<Obj> findById(String id) {
		List<Obj> ret=objects.getPossibleDefault(id, new ArrayList<>());
		return new ArrayList<>(ret);
	}
	/**
	 * Find objects by type.
	 * @param id
	 * @return all matches. The returned object is an unmodifiable set wrapped version of the internal storage. 
	 */
	public Set<Obj> findByType(String type) {
		HashSet<Obj> ret=objectsByType.getPossibleDefault(type, new HashSet<Obj>());
		return Collections.unmodifiableSet(ret);
	}
	public String getTypeAndLocalId(String type, String localId) {
		return ""+type+"."+localId;
	}
	/**
	 * Get the set of all references.
	 * @return the internal set - do not modify and do not store until it may be changed by the owner!
	 */
	public Set<Ref> getRefs() {
		return refs;
	}
	/**
	 * Not to be used in production code!
	 * Debug feature to re-execute a single unresolved reference
	 * to be able to debug it.
	 * Call closeTransaction immediately after this call!
	 * @param r
	 */
	public void signalReresolve(Ref r) {
		toResolve.add(r);
	}
	/**
	 * Get the internal storage of objects.
	 * Handle with care! (dont modify and don't use after and CRM method is called)
	 * @return
	 */
	public MultiMapTreeImpl<String, Obj> getObjects() {
		return objects;
	}
	/**
	 * Free to all cache.
	 */
	public MultiMapHashToHashSetImpl<String, Object> caches=new MultiMapHashToHashSetImpl<>();
	/**
	 * Called when a document has change notifications.
	 * @param doc
	 */
	public void registerChangedDoc(Doc doc) {
		synchronized (syncObj)
		{
			changedDocs.add(doc);
		}
	}
}
