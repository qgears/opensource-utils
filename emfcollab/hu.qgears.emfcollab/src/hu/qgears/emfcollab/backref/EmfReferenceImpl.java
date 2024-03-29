package hu.qgears.emfcollab.backref;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import hu.qgears.emfcollab.backref.EmfBackReferenceImpl.EmfObjectReferencesAdapter;

/**
 * Wrapper class for storing source, refType, target trio.
 * 
 * Manages hashCode and equals so instances can be stored in a hashmap.
 *
 */
public class EmfReferenceImpl implements EmfReference {
	private EReference refType;
	private EObject source;
	private EObject target;
	private EmfObjectReferencesAdapter sourceAdapter;
	private EmfObjectReferencesAdapter targetAdapter;
	private int hashCode;
	@Override
	public int hashCode() {
		return hashCode;
	}
	public EReference getRefType() {
		return refType;
	}
	public EObject getSource() {
		return source;
	}
	public EObject getTarget() {
		return target;
	}
	public EmfReferenceImpl(EmfObjectReferencesAdapter sourceAdapter, EReference refType, EmfObjectReferencesAdapter targetAdapter) {
		super();
		this.sourceAdapter=sourceAdapter;
		this.targetAdapter=targetAdapter;
		this.source = sourceAdapter.getEObject();
		this.refType = refType;
		this.target = targetAdapter.getEObject();
		this.hashCode=source.hashCode()^
			refType.hashCode()^
			target.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EmfReferenceImpl)
		{
			EmfReferenceImpl other=(EmfReferenceImpl) obj;
			return
				other.hashCode==this.hashCode&&
				other.source.equals(this.source)&&
				other.refType.equals(this.refType)&&
				other.target.equals(this.target);
		}
		return super.equals(obj);
	}
	public EmfObjectReferencesAdapter getSourceAdapter() {
		return sourceAdapter;
	}
	public EmfObjectReferencesAdapter getTargetAdapter() {
		return targetAdapter;
	}
}
