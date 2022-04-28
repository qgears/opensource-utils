package hu.qgears.xtextgrammar;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.crossref.CrossRefManager;
import hu.qgears.crossref.Doc;
import hu.qgears.crossref.IRefListener;
import hu.qgears.crossref.Obj;
import hu.qgears.crossref.Ref;
import hu.qgears.crossref.Scope;

/**
 * A cross reference in the text file.
 * The cross reference has these states:
 *  * {@link Ref} object is not created yet.
 *    (This is possible when the identifier of the reference depends on other objects to be resolved.
 *     For example the scope of the reference depends on an other reference being resolved.)
 *  * {@link Ref} object is created but not resolved yet.
 *    (This is possible when target does not exist or when resolve cycle was not executed yet)
 *    (When a reference has multiple resolve targets found then it is kept in this state.)
 *  * {@link Ref} object is resolved.
 */
public class CrossReferenceInstance implements IRefListener {
	public Set<String> unresolvedReferenceAcceptedTypes;
	public String unresolvedReferenceRawRecerenceString;
	public String unresolvedReferenceType;
	public CrossReferenceAdapter targetA;
	public CrossReferenceAdapter source;
	public EReference r;
	public int index;
	/**
	 * In case the reference is created then it is stored here.
	 */
	public Ref ref;
	/**
	 * In case this is resolved then this value is set.
	 */
	public CrossReferenceAdapter resolvedToAdapter;
	/**
	 * When this cross reference goes to unresolved state then this EObject is used as a placeholder for the reference.
	 */
	public CrossReferenceAdapter unresolvedObjectAdapter;
	/**
	 * True means reference is not in  resolved state because multiple targets were found.
	 */
	public boolean multiTargetError;
	/**
	 * In case this is set to true then this cross reference is intentionally unset and should be omitted by validator to not raise an error.
	 */
	public boolean intentionallyUnresolved=false;
	/**
	 * Trackable storage of resolved status.
	 */
	private UtilListenableProperty<Boolean> resolvedStatus;
	private UtilListenableProperty<CrossReferenceAdapter> currentTarget;
	public CrossReferenceInstance(CrossReferenceAdapter host) {
		this.targetA=host;
	}
	@Override
	public void resolvedTo(List<Obj> target) {
		CrossReferenceAdapter prev=resolvedToAdapter;
		if(target==null || target.size()>1)
		{
			source.setReferenceTarget(this, r, index, resolvedToAdapter, null, (EObject)unresolvedObjectAdapter.getTarget());
			resolvedToAdapter=null;
			multiTargetError=target!=null;
			if(resolvedStatus!=null)
			{
				resolvedStatus.setProperty(false);
			}
			if(currentTarget!=null)
			{
				currentTarget.setProperty(unresolvedObjectAdapter);
			}
		}else
		{
			Obj tg=target.get(0);
			resolvedToAdapter=(CrossReferenceAdapter)tg.getUserObject(null);
			source.setReferenceTarget(this, r, index, prev, resolvedToAdapter, (EObject)resolvedToAdapter.getTarget());
			multiTargetError=false;
			if(resolvedStatus!=null)
			{
				getReadyStatus().addSubProperty(resolvedToAdapter.getObjectReadyCalculator());
				resolvedStatus.setProperty(true);
			}
			if(currentTarget!=null)
			{
				currentTarget.setProperty(resolvedToAdapter);
			}
		}
		if(prev!=null)
		{
			getReadyStatus().addSubProperty(prev.getObjectReadyCalculator());
		}
	}
	public UtilListenableProperty<Boolean> getResolvedStatus() {
		if(resolvedStatus==null)
		{
			resolvedStatus=new UtilListenableProperty<>(resolvedToAdapter!=null);
		}
		return resolvedStatus;
	}
	private MultiStateCollector readyStatus;
	/**
	 * The object is resolved _and_ also ready in case of unflolding is done.
	 * @return Can be used either to listen to or to delegate additional sub properties
	 */
	public MultiStateCollector getReadyStatus() {
		if(readyStatus==null)
		{
			readyStatus=new MultiStateCollector();
			readyStatus.addSubProperty(getResolvedStatus());
		}
		return readyStatus;
	}
	public boolean isUnresolved() {
		return resolvedToAdapter==null;
	}
	public CrossReferenceInstance setUnresolvedReference(String prefixproxyid, String unescape) {
		unresolvedReferenceRawRecerenceString=unescape;
		unresolvedReferenceType=prefixproxyid;
		return this;
	}
	public void setSourceParameters(EObject src, EReference r, int index) {
		this.source=targetA.getOrCreateForObject(src);
		source.addManagedReference(r, index, this);
		this.r=r;
		this.index=index;
	}
	@SuppressWarnings("unchecked")
	public void setReferenceSearchScope(Scope scope) {
		if(ref!=null)
		{
			ref.close();
			ref.removeListener(this);
			ref=null;
			resolvedTo(null);
		}
		if(scope==null)
		{
			scope=new Scope(null, "NOT_SET:"+unresolvedReferenceRawRecerenceString, null, null);
			scope.setAllowedTypes(Collections.EMPTY_SET);
			Doc doc=unresolvedObjectAdapter.getDoc();
			CrossRefManager crm=doc.getHost();
			ref=crm.createRef(doc, scope);
			ref.addListener(this);
			ref.setUserObject(null, this);
			return;
		}else
		{
			scope.setAllowedTypes(unresolvedReferenceAcceptedTypes);
			Doc doc=unresolvedObjectAdapter.getDoc();
			CrossRefManager crm=doc.getHost();
			ref=crm.createRef(doc, scope);
			ref.addListener(this);
			ref.setUserObject(null, this);
		}
		targetA.registerUnresolvedSourceCrossRef(this);
	}
	@Override
	public String toString() {
		if(resolvedToAdapter!=null)
		{
			return ""+r.getName()+" "+resolvedToAdapter;
		}else
		{
			if(ref==null)
			{
				return ""+r.getName()+" UNSET "+unresolvedReferenceRawRecerenceString;
			}else
			{
				return ""+r.getName()+" "+ref.getScope();
			}
		}
	}
	public void setFeatureThatEndsInThis(EStructuralFeature currentFeature) {
		if(currentFeature instanceof EReference)
		{
			r=(EReference) currentFeature;
		}else
		{
			// TODO how does it happen?
		}
	}
	public void referenceInstalledIntoTree(EObject host, EStructuralFeature r, int finalIndex)
	{
		throw new RuntimeException("Must be overriden");
	}
	public UtilListenableProperty<CrossReferenceAdapter> getCurrentTarget() {
		if(currentTarget==null)
		{
			currentTarget=new UtilListenableProperty<>();
			currentTarget.setProperty(getCurrentTargetValue());
		}
		return currentTarget;
	}
	private CrossReferenceAdapter getCurrentTargetValue() {
		if(resolvedToAdapter!=null)
		{
			return resolvedToAdapter;
		}else
		{
			return unresolvedObjectAdapter;
		}
	}
	@SuppressWarnings("unchecked")
	public void duplicateThisReferenceTo(EObject newSourceObject, EReference eReference, int i) {
		EObject tg=(EObject)unresolvedObjectAdapter.getTarget();
		// EObject newTg=tg.eClass().getEPackage().getEFactoryInstance().create();
		CrossReferenceAdapter cra=source.createNewUnresolvedReferenceTargetPlaceHolder(tg.eClass());
		EObject newTg=(EObject)cra.getTarget();
		if(eReference.isMany())
		{
			((List<EObject>)newSourceObject.eGet(eReference)).add(newTg);
		}else
		{
			newSourceObject.eSet(eReference, newTg);
		}
		CrossReferenceInstance cri=cra.getOrCreateUnresolvedCrossReferenceObject();
		cri.setFeatureThatEndsInThis(eReference);
		cri.setSourceParameters(newSourceObject, eReference, i);
		cri.unresolvedReferenceAcceptedTypes=unresolvedReferenceAcceptedTypes;
		cri.unresolvedReferenceType=unresolvedReferenceType;
		cri.unresolvedReferenceRawRecerenceString=unresolvedReferenceRawRecerenceString;
		cri.source.getAddedToTreeProperty().addListenerWithInitialTrigger(new UtilEventListener<Boolean>() {
			@Override
			public void eventHappened(Boolean b) {
				if(b)
				{
					cri.referenceInstalledIntoTree(newSourceObject, eReference, i);
				}else
				{
					// cri.setReferenceSearchScope(null);
				}
			}
		});
	}
	private Object debugDynamicResolve;
	public void setDebugDynamicResolve(Object doubleReferenceListener) {
		this.debugDynamicResolve=doubleReferenceListener;
	}
	public Object getDebugDynamicResolve() {
		return debugDynamicResolve;
	}
}
