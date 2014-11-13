package hu.qgears.emfcollab.editor.view;

import hu.qgears.emfcollab.editor.EmfCommandWrapper;

import org.eclipse.jface.viewers.LabelProvider;


public class HistoryLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if(element instanceof EmfCommandWrapper)
		{
			EmfCommandWrapper ecw=(EmfCommandWrapper) element;
			return ""+ecw.getCommand().getOwner().getUserName()+": "+ecw.getCommand().getName();
		}
		return ""+element;
	}
}
