package hu.qgears.xtextgrammar;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.crossref.CrossRefManager;
import hu.qgears.crossref.Doc;
import hu.qgears.crossref.Obj;

/**
 * An object that keeps track of all proxy object references on an EObject.
 */
public class CrossReferenceAdapter implements Adapter {
	private EObject host;
	protected Doc doc;
	/**
	 * In case this object delegates a referencable object to crossref then that is stored here.
	 */
	private Obj nameObject;
	/**
	 * Source code reference that created this object.
	 */
	private SourceReference sourceReference;
	/**
	 * In case this is an unresolved reference placeholder (like a proxy but not eIsProxy()) object
	 * then this object is not nutt.
	 */
	private CrossReferenceInstance proxyCrossReference;
	private UtilEvent<CrossReferenceAdapter> managedCrossReferenceListChangedEvent;
	/**
	 * Store all source cross references of the host object.
	 */
	public CrossReferenceAdapter(EObject o) {
		this.host=o;
		if(getAllowNull(o)!=null)
		{
			throw new RuntimeException();
		}
		o.eAdapters().add(this);
		EObject c=o.eContainer();
		if(c!=null)
		{
			CrossReferenceAdapter cra=CrossReferenceAdapter.getAllowNull(c);
			if(cra!=null)
			{
				doc=cra.getDoc();
			}
		}
	}
	public CrossReferenceInstance getOrCreateUnresolvedCrossReferenceObject()
	{
		if(proxyCrossReference==null)
		{
			proxyCrossReference=createNewCrossReferenceInstance();
			proxyCrossReference.unresolvedObjectAdapter=this;
		}
		return proxyCrossReference;
	}
	protected CrossReferenceInstance createNewCrossReferenceInstance()
	{
		return new CrossReferenceInstance(this);
	}
	public CrossReferenceAdapter getOrCreateForObject(EObject eo)
	{
		return CrossReferenceAdapter.get(eo);
	}
	@Override
	public void notifyChanged(Notification notification) {
	}
	@Override
	public Notifier getTarget() {
		return host;
	}
	@Override
	public void setTarget(Notifier newTarget) {
	}
	@Override
	public boolean isAdapterForType(Object type) {
		return false;
	}
	public static CrossReferenceAdapter get(EObject o) {
		EList<Adapter> adapters=o.eAdapters();
		for(Adapter a: adapters)
		{
			if(a instanceof CrossReferenceAdapter)
			{
				return (CrossReferenceAdapter)a;
			}
		}
		CrossReferenceAdapter ret=new CrossReferenceAdapter(o);
		return ret;
	}
	protected void registerUnresolvedSourceCrossRef(CrossReferenceInstance cri) {
	}
	public void registerName(String name) {
		Doc doc=getDoc();
		if(nameObject!=null)
		{
			nameObject.close();
			nameObject=null;
		}
		CrossRefManager crm=doc.getHost();
		nameObject=crm.createObj(doc, name, RuntimeMappings.getEMFClassName(host.eClass()));
		nameObject.setUserObject(null, this);
	}
	public Doc getDoc() {
		return doc;
	}
	public CrossReferenceAdapter setDoc(Doc doc) {
		this.doc = doc;
		return this;
	}
	public void printNames(String prefix, Writer fw) throws IOException {
		if(nameObject!=null)
		{
			fw.write(" ");
			fw.write("NAME: "+nameObject.getFqId()+" "+nameObject.getType());
//			fw.write("\n");
		}
		List<EReference> refs=host.eClass().getEAllReferences();
		for(EReference r: refs)
		{
			if(!r.isTransient() && !r.isDerived())
			{
				Object val=host.eGet(r);
				if(val instanceof EObject)
				{
					printIfProxyTg(prefix, fw, (EObject) val);
				}else if(val instanceof List<?>)
				{
					List<?> l=(List<?>) val;
					for(Object tg: l)
					{
						if(tg instanceof EObject)
						{
							printIfProxyTg(prefix, fw, (EObject) tg);
						}
					}
				}
			}
		}
	}
	private void printIfProxyTg(String prefix, Writer fw, EObject val) throws IOException {
		CrossReferenceAdapter pa=CrossReferenceAdapter.getAllowNull(val);
		if(pa!=null)
		{
			CrossReferenceInstance cri=pa.getUnresolvedCrossReference();
			if(cri!=null)
			{
				if(cri.resolvedToAdapter==null)
				{
					if(cri.intentionallyUnresolved)
					{
						fw.write(" {INTENTIONALLY_BLANK}");
					} else if(cri.ref!=null)
					{
						fw.write(" {UNRESOLVED "+(cri.multiTargetError?"MULTIERR ":"NONE ")+ cri.r.getName()+" "+cri.ref.getScope().getId()+" "+cri.ref.getScope().getLocalIdentifier()+"Types: "+cri.ref.getScope().getAllowedTypes()+"}");
					} else
					{
						fw.write(" {UNRESOLVED REFId Not set"+ cri.r.getName()+"}");
					}
				}else
				{
					fw.write(" {RESOLVED}");
				}
			}
		}
	}
	public static CrossReferenceAdapter getAllowNull(EObject o) {
		EList<Adapter> adapters=o.eAdapters();
		for(Adapter a: adapters)
		{
			if(a instanceof CrossReferenceAdapter)
			{
				return (CrossReferenceAdapter)a;
			}
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public void setReferenceTarget(CrossReferenceInstance cri, EReference r, int index, CrossReferenceAdapter prev, 
			CrossReferenceAdapter resolvedToAdapter,
			EObject eObject) {
		// TODO maintain cross reference caches!
		if(r.isMany())
		{
			List<EObject> l=null;
			try {
				l=(List<EObject>)host.eGet(r);
				l.set(index, eObject);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.err.println("Error details host: "+host);
				System.err.println("Error details ref: "+r);
				System.err.println("Error details prev list: "+l);
				System.err.println("Error details to set: "+index+" "+eObject);
				System.err.println("Error details resource: "+host.eResource().getURI());
				e.printStackTrace();
			}
		}else
		{
			host.eSet(r, eObject);
		}
		if(prev!=null)
		{
			registerUnresolvedSourceCrossRef(cri);
		}
		if(resolvedToAdapter!=null)
		{
			registerResolvedSourceCrossRef(cri);
		}
	}
	protected void registerResolvedSourceCrossRef(CrossReferenceInstance cri) {
	}
	private UtilListenableProperty<Boolean> parentProperty=null;
	public void notifyAttachEvent(TrackerAdapter trackerAdapter) {
		Notifier n=getTarget();
		if(n instanceof EObject)
		{
			Resource r=((EObject)getTarget()).eResource();
			if(parentProperty!=null)
			{
				parentProperty.setProperty(r!=null);
			}
		}
	}
	public void notifyAttachEventInitial(TrackerAdapter trackerAdapter) {
		Notifier n=getTarget();
		if(n instanceof EObject)
		{
			Resource r=((EObject)getTarget()).eResource();
			if(parentProperty!=null)
			{
				parentProperty.setProperty(r!=null);
			}
		}
	}
	public CrossReferenceInstance getUnresolvedCrossReference() {
		return proxyCrossReference;
	}
	
	MultiStateCollector objectReady;
	public MultiStateCollector getObjectReadyCalculator() {
		if(objectReady==null)
		{
			objectReady=new MultiStateCollector();
		}
		return objectReady;
	}
	public void setSourceReference(SourceReference sourceReference) {
		this.sourceReference=sourceReference;
	}
	public SourceReference getSourceReference() {
		return sourceReference;
	}
	public boolean isUnresolvedReference() {
		return proxyCrossReference!=null&&proxyCrossReference.isUnresolved();
	}
	public UtilListenableProperty<Boolean> getAddedToTreeProperty() {
		if(parentProperty==null)
		{
			parentProperty=new UtilListenableProperty<>();
			notifyAttachEventInitial(null);
		}
		return parentProperty;
	}
	public List<CrossReferenceInstance> managedReferences=new ArrayList<CrossReferenceInstance>();
	public void addManagedReference(EReference r, int index, CrossReferenceInstance crossReferenceInstance) {
		managedReferences.add(crossReferenceInstance);
		if(managedCrossReferenceListChangedEvent!=null)
		{
			managedCrossReferenceListChangedEvent.eventHappened(this);
		}
	}
	public CrossReferenceInstance getManagedReference(EReference r, Integer idx) {
		for(CrossReferenceInstance c: managedReferences)
		{
			if(c.r==r && (idx==null || idx==c.index))
			{
				return c;
			}
		}
		return null;
	}
	public CrossReferenceAdapter createNewUnresolvedReferenceTargetPlaceHolder(EClass eClass) {
		EObject ret=eClass.getEPackage().getEFactoryInstance().create(eClass);
		CrossReferenceAdapter cra=createNewAdapter(ret);
		cra.setDoc(doc);
		return cra;
	}
	protected CrossReferenceAdapter createNewAdapter(EObject o) {
		return new CrossReferenceAdapter(o);
	}
	public CrossReferenceAdapter copyMetadataOnto(EObject ret) {
		CrossReferenceAdapter cra=getOrCreateForObject(ret);
		cra.setDoc(doc);
		cra.sourceReference=sourceReference;
		return cra;
	}
	public Obj getNameObject() {
		return nameObject;
	}
	public UtilEvent<CrossReferenceAdapter> getManagedCrossReferenceListChangedEvent() {
		if(managedCrossReferenceListChangedEvent==null)
		{
			managedCrossReferenceListChangedEvent=new UtilEvent<>();
		}
		return managedCrossReferenceListChangedEvent;
	}
}
