package hu.qgears.emfcollab.editor.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class HistoryContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement!=null)
		{
			if(parentElement instanceof List<?>)
			{
				List<Object>ret=new ArrayList<Object>((List<?>)parentElement);
				Collections.reverse(ret);
				return ret.toArray();
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] ch=getChildren(element);
		return ch!=null&&ch.length>0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
//		return new Object[]{inputElement};
	}

}
