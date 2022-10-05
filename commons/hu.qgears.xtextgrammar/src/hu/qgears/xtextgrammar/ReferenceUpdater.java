package hu.qgears.xtextgrammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import hu.qgears.commons.Pair;
import hu.qgears.emfcollab.backref.EmfBackReferenceImpl;
import hu.qgears.emfcollab.backref.EmfObjectReferences;

public class ReferenceUpdater <T extends EObject> {
	private EmfBackReferenceImpl ebr;
	private CRAEReference cri;
	private List<Pair<EObject, EReference>> musts=new ArrayList<Pair<EObject,EReference>>();
	private T host;
	public ReferenceUpdater(EmfBackReferenceImpl ebr, CRAEReference cri, Class<T> class1, EObject host) {
		this.ebr=ebr;
		this.cri=cri;
		this.host=class1.cast(host);
	}
	public ReferenceUpdater<T> addMust(EObject eo, EReference r)
	{
		musts.add(new Pair<EObject, EReference>(eo, r));
		return this;
	}
	public void install(ReferenceUpdaterCallback<T> ruc)
	{
		for(Pair<EObject, EReference> p: musts)
		{
			EmfObjectReferences or=ebr.getEmfReferencesAdapter(p.getA());
			or.addReferenceListener(p.getB(), e->{
				update(ruc);
			}, false);
		}
		update(ruc);
	}
	private void update(ReferenceUpdaterCallback<T> ruc) {
		for(Pair<EObject, EReference> p: musts)
		{
			Object v=p.getA().eGet(p.getB());
			if(v==null)
			{
				cri.setReferenceSearchScope(null);
				return;
			}else
			{
				CRAEObject cra=CRAEObject.getAllowNull((EObject)v);
				if(cra==null||cra.isUnresolvedReference())
				{
					cri.setReferenceSearchScope(null);
					return;
				}
			}
		}
		ruc.updateReference(host);
	}
}
