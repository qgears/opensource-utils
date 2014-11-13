package hu.qgears.emfcollab.editor.view;

import hu.qgears.emfcollab.editor.EmfCollabEditorExtension;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;


public class HistoryPartListener implements IPartListener {
	HistoryView view;
	public HistoryPartListener(HistoryView view) {
		super();
		this.view = view;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		EmfCollabEditorExtension editor=(EmfCollabEditorExtension)part.getAdapter(EmfCollabEditorExtension.class);
		if(editor!=null)
		{
			view.setEditor(editor);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
	}

}
