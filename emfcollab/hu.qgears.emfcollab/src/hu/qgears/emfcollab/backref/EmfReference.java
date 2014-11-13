package hu.qgears.emfcollab.backref;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/**
 * Wrapper class for storing source, refType, target trio.
 * 
 * Manages hashCode and equals so instances can be stored in a hashmap.
 * 
 * @author rizsi
 *
 */
public class EmfReference {
	private EReference refType;
	private EObject source;
	private EObject target;
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
	public EmfReference(EObject source, EReference refType, EObject target) {
		super();
		this.source = source;
		this.refType = refType;
		this.target = target;
		this.hashCode=source.hashCode()^
			refType.hashCode()^
			target.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EmfReference)
		{
			EmfReference other=(EmfReference) obj;
			return
				other.hashCode==this.hashCode&&
				other.source.equals(this.source)&&
				other.refType.equals(this.refType)&&
				other.target.equals(this.target);
		}
		return super.equals(obj);
	}
}
