package hu.qgears.parser.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import hu.qgears.xtextgrammar.ResourceCrossReferenceAdapter;

public class QEdContentProvider implements ITreeContentProvider {
	private Object input;
	private Object[] empty=new Object[]{};
	@Override
	public void dispose() {
	}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input=newInput;
	}
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof Object[])
		{
			return (Object[]) parentElement;
		}else if(parentElement instanceof ResourceSet)
		{
			ResourceSet rs=(ResourceSet) parentElement;
			synchronized (rs) {
				List<Resource> ret=new ArrayList<Resource>(rs.getResources());
				Collections.sort(ret, new Comparator<Resource>() {
					@Override
					public int compare(Resource o1, Resource o2) {
						ResourceCrossReferenceAdapter a1=ResourceCrossReferenceAdapter.get(o1);
						ResourceCrossReferenceAdapter a2=ResourceCrossReferenceAdapter.get(o2);
						return a1.getDocId().compareTo(a2.getDocId());
					}
				});
				return ret.toArray(empty);
			}
		}
		if(parentElement instanceof Resource)
		{
			Resource r=(Resource) parentElement;
			synchronized (r.getResourceSet()) {
				return r.getContents().toArray(empty);
			}
		}
		if(parentElement instanceof EObject)
		{
			EObject o=(EObject) parentElement;
			synchronized (o.eResource().getResourceSet()) {
				List<Object> ret=new ArrayList<>(o.eContents());
				for(EReference r:o.eClass().getEAllReferences())
				{
					if(!r.isContainment() && !r.isDerived())
					{
						Object v=o.eGet(r);
						if(v instanceof List<?>)
						{
							for(Object tg: ((List<?>)v))
							{
								ret.add(new RefInTree(o, r, (EObject)tg));
							}
						}else if(v instanceof EObject)
						{
							ret.add(new RefInTree(o, r, (EObject)v));
						}
					}
				}
				return ret.toArray(empty);
			}
		}
		return empty;
	}
	@Override
	public Object getParent(Object element) {
		if(element instanceof Resource)
		{
			return ((Resource) element).getResourceSet();
		}else if(element instanceof EObject)
		{
			EObject eo=((EObject) element);
			if(eo.eContainer()!=null)
			{
				return eo.eContainer();
			}else
			{
				return eo.eResource();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length>0;
	}
}
