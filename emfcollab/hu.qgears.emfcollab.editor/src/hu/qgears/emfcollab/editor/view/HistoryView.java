package hu.qgears.emfcollab.editor.view;

import hu.qgears.coolrmi.UtilEvent;
import hu.qgears.emfcollab.editor.EmfCollabCommandStack;
import hu.qgears.emfcollab.editor.EmfCollabEditorExtension;
import hu.qgears.emfcollab.editor.EmfCommandWrapper;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;


public class HistoryView extends ViewPart {
	private EmfCollabEditorExtension currentEditor;
	private CommandStackListener li = new CommandStackListener() {

		@Override
		public void commandStackChanged(EventObject event) {
			updateView(false);
		}
	};
	TableViewer undoTable;
	TableViewer redoTable;
	List<Button> buttons=new ArrayList<Button>();
	IWorkbenchPage page;
	UtilEvent<EmfCollabEditorExtension> disposeListener=new UtilEvent<EmfCollabEditorExtension>(){
		@Override
		public void eventHappened(EmfCollabEditorExtension msg) {
			setEditor(null);
		}
	};

	public HistoryView() {
	}

	HistoryPartListener partListener = new HistoryPartListener(this);

	class UndoTop extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				currentEditor.getCommandStack().undoTop();
			}
		}
	}
	class UndoMine extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				currentEditor.getCommandStack().undo();
			}
		}
	}
	class RedoMine extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				currentEditor.getCommandStack().redo();
			}
		}
	}

	class RedoTop extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				currentEditor.getCommandStack().redoTop();
			}
		}
	}

	class RedoSelected extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				ISelection sel = redoTable.getSelection();
				if (sel instanceof StructuredSelection) {
					StructuredSelection s = (StructuredSelection) sel;
					Object o = s.getFirstElement();
					if (o instanceof EmfCommandWrapper) {
						currentEditor.getCommandStack().redo(
								(EmfCommandWrapper) o);
					}
				}
			}
		}
	}

	class UndoSelected extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				ISelection sel = undoTable.getSelection();
				if (sel instanceof StructuredSelection) {
					StructuredSelection s = (StructuredSelection) sel;
					Object o = s.getFirstElement();
					if (o instanceof EmfCommandWrapper) {
						currentEditor.getCommandStack().undo(
								(EmfCommandWrapper) o);
					}
				}
			}
		}
	}

	class Commit extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				CommitLogDialog d = new CommitLogDialog(getSite().getShell(),
						"Enter Commit log");
				if (d.open() == CommitLogDialog.OK) {
					String log = d.getCommitlog();
					currentEditor.commit(log);
				}
			}
		}
	}
	class DisposeServer extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				MessageDialog md=new MessageDialog(
						getSite().getShell(), 
						"Dispose EMF server", null,
						"Are you sure?", 0,
						new String[]{"Ok", "Cancel"}, 0);
				if(md.open()==MessageDialog.OK)
				{
					try {
						currentEditor.disposeServrSideModel(true);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
	class UpdateServer extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				MessageDialog md=new MessageDialog(
						getSite().getShell(), 
						"Update EMF server", null,
						"Are you sure?", 0,
						new String[]{"Ok", "Cancel"}, 0);
				if(md.open()==MessageDialog.OK)
				{
					try {
						currentEditor.disposeServrSideModel(false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
	class RevertServer extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (currentEditor != null) {
				MessageDialog md=new MessageDialog(
						getSite().getShell(), 
						"Revert EMF server", null,
						"Are you sure?", 0,
						new String[]{"Ok", "Cancel"}, 0);
				if(md.open()==MessageDialog.OK)
				{
					try {
						currentEditor.revertServerSideModel();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		// TabFolder tabFolder = new TabFolder(parent, SWT.NO);
		// TabItem tabUndo = new TabItem(tabFolder, SWT.NULL);
		// tabUndo.setText("Undo");
		Composite sashForm = new Composite(parent, SWT.NO);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		sashForm.setLayout(layout);
		// SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		Composite buttons = new Composite(sashForm, SWT.NO);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.makeColumnsEqualWidth = false;
		buttons.setLayout(gridLayout);
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Undo mine");
			b.addSelectionListener(new UndoMine());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Redo mine");
			b.addSelectionListener(new RedoMine());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Undo top");
			b.addSelectionListener(new UndoTop());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Undo Selected");
			b.addSelectionListener(new UndoSelected());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Redo top");
			b.addSelectionListener(new RedoTop());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Redo Selected");
			b.addSelectionListener(new RedoSelected());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Commit Version");
			b.addSelectionListener(new Commit());
			this.buttons.add(b);
		}
		/*
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Dispose server");
			b.addSelectionListener(new DisposeServer());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Update server");
			b.addSelectionListener(new UpdateServer());
			this.buttons.add(b);
		}
		{
			Button b = new Button(buttons, SWT.NO);
			b.setText("Revert server");
			b.addSelectionListener(new RevertServer());
			this.buttons.add(b);
		}
		*/
		enableButtons(false);
		page = getSite().getPage();
		page.addPartListener(partListener);

		GridData gd6 = new GridData();
		gd6.horizontalSpan = 1;
		gd6.verticalSpan = 1;
		gd6.horizontalAlignment = GridData.FILL;
		gd6.verticalAlignment = GridData.FILL;
		gd6.grabExcessHorizontalSpace = true;
		gd6.grabExcessVerticalSpace = true;

		SashForm v = new SashForm(sashForm, SWT.HORIZONTAL);
		v.setLayoutData(gd6);
		Composite c = new Composite(v, SWT.NO);
		c.setLayout(new FillLayout());

		undoTable = new TableViewer(c, SWT.SINGLE);
		undoTable.setLabelProvider(new HistoryLabelProvider());
		undoTable.setContentProvider(new HistoryContentProvider());

		redoTable = new TableViewer(v, SWT.SINGLE);
		redoTable.setLabelProvider(new HistoryLabelProvider());
		redoTable.setContentProvider(new HistoryContentProvider());
		// sashForm.setWeights(new int[]{1,10});
		// TODO init with currently open window
	}

	@Override
	public void dispose() {
		setEditor(null);
		page.removePartListener(partListener);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	public void setEditor(EmfCollabEditorExtension editor) {
		if (undoTable.getControl().isDisposed()) {
			return;
		}
		if (editor != currentEditor) {
			if (currentEditor != null) {
				currentEditor.getDiposeEvent().removeListener(disposeListener);
				currentEditor.getCommandStack().removeCommandStackListener(li);
				currentEditor = null;
			}
			currentEditor = editor;
			if (editor != null) {
				EmfCollabCommandStack cs = editor.getCommandStack();
				cs.addCommandStackListener(li);
				currentEditor.getDiposeEvent().addListener(disposeListener);
			}
			enableButtons(editor!=null);
			updateView(true);
		}
	}

	private void enableButtons(boolean flag) {
		for(Button b: buttons)
		{
			b.setEnabled(flag);
		}
	}

	protected void updateView(boolean editorChanged) {
		if (undoTable.getControl().isDisposed()) {
			return;
		}
		if (editorChanged) {
			if (currentEditor != null) {
				undoTable.setInput(currentEditor.getCommandStack()
						.getUndoList());
				redoTable.setInput(currentEditor.getCommandStack()
						.getRedoList());
			} else {
				undoTable.setInput(null);
				redoTable.setInput(null);
			}
		} else {
			undoTable.refresh();
			redoTable.refresh();
		}
	}
}
