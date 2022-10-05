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

import hu.qgears.xtextgrammar.CRAResource;

public class QEdContentProvider implements ITreeContentProvider {
	@SuppressWarnings("unused")
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
						CRAResource a1=CRAResource.get(o1);
						CRAResource a2=CRAResource.get(o2);
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
							int index=0;
							for(Object tg: ((List<?>)v))
							{
								ret.add(RefInTree.create(o, r, (EObject)tg, index));
								index++;
							}
						}else if(v instanceof EObject)
						{
							ret.add(RefInTree.create(o, r, (EObject)v, 0));
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
