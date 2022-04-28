package hu.qgears.parser.editor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;

public class QEditorOutlinePage extends ContentOutlinePage {
	private IEditorInput editorInput;
	private UtilListenableProperty<AbstractBuilder> builder;
	private Object nullInput=new Object[]{"Model not parsed yet - enable builder to see outline"};
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(createContentProvider());
		viewer.setLabelProvider(createLabelProvider());
		viewer.addSelectionChangedListener(this);
		updateInput();
		if(builder!=null && builder.getProperty()!=null)
		{
			updateView(builder.getProperty().buildResult.getProperty());
		}
	}
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		QEditorSelectionSingleton.getInstance().outlineSelectionEvent.eventHappened(event);
	}
	@Override
	public void setSelection(ISelection selection) {
		super.setSelection(selection);
	}
	private void updateInput() {
		if(builder==null)
		{
			getTreeViewer().setInput(nullInput);
		}else
		{
			AbstractBuilder bld=builder.getProperty();
			if(bld!=null)
			{
				ResourceSet rs=bld.buildResult.getProperty();
				if(rs!=null)
				{
					getTreeViewer().setInput(rs);
				}else
				{
					getTreeViewer().setInput(nullInput);
				}
			}else
			{
				getTreeViewer().setInput(nullInput);
			}
		}
	}
	/**
	 * Override to change default tree content behaviour.
	 * @return
	 */
	protected IContentProvider createContentProvider() {
		return new QEdContentProvider();
	}
	/**
	 * Override to change default label provider behaviour.
	 * @return
	 */
	protected CellLabelProvider createLabelProvider()
	{
		return new QEdLabelProvider();
	}
	public void setEditorInput(IEditorInput input) {
		this.editorInput=input;
	}
	private AbstractBuilder current;
	private UtilEventListener<ResourceSet> buildResultListener=new UtilEventListener<ResourceSet>() {
		@Override
		public void eventHappened(ResourceSet msg) {
			updateView(msg);
		}
	};
	private UtilEventListener<ResourceSet> modelUpdatedListener=new UtilEventListener<ResourceSet>() {
		@Override
		public void eventHappened(ResourceSet msg) {
			TreeViewer tv=getTreeViewer();
			if(tv!=null)
			{
				if(tv.getControl().isDisposed())
				{
					return;
				}
				tv.getControl().getDisplay().asyncExec(()->{
					synchronized (msg) {
						getTreeViewer().refresh();
					}
				});
			}
		}
	};
	NoExceptionAutoClosable toClose;
	public void setBuilder(UtilListenableProperty<AbstractBuilder> builder) {
		this.builder=builder;
		if(toClose!=null)
		{
			toClose.close();
			toClose=null;
			if(current!=null)
			{
				current.buildResult.getPropertyChangedEvent().removeListener(buildResultListener);
				current.modelUpdated.removeListener(modelUpdatedListener);
				current=null;
			}
		}
		if(builder!=null)
		{
			toClose=builder.addListenerWithInitialTrigger(bld->{
				if(current!=null)
				{
					current.buildResult.getPropertyChangedEvent().removeListener(buildResultListener);
					current.modelUpdated.removeListener(modelUpdatedListener);
					current=null;
				}
				if(bld!=null)
				{
					current=bld;
					current.buildResult.addListenerWithInitialTrigger(buildResultListener);
					current.modelUpdated.addListener(modelUpdatedListener);
				}
			});
		}
	}
	protected void updateView(ResourceSet msg) {
		TreeViewer tv=getTreeViewer();
		if(tv!=null)
		{
			tv.getControl().getDisplay().asyncExec(()->{
				synchronized (msg) {
					getTreeViewer().setInput(msg);
				}
			});
		}
	}
	@Override
	public void dispose() {
		setBuilder(null);
		super.dispose();
	}
	public void setSelectedEmfObject(EObject eo) {
		if(getTreeViewer()!=null)
		{
			getTreeViewer().setSelection(new StructuredSelection(eo), true);
		}
	}
}
