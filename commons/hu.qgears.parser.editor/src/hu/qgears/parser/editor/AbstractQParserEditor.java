package hu.qgears.parser.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.emfcollab.util.UtilVisitor;
import hu.qgears.parser.coloring.StyleBasedColoring;
import hu.qgears.parser.editor.coloring.SwtStyleBasedColoring;
import hu.qgears.xtextgrammar.CrossReferenceAdapter;
import hu.qgears.xtextgrammar.ResourceCrossReferenceAdapter;
import hu.qgears.xtextgrammar.SourceReference;

abstract public class AbstractQParserEditor extends AbstractDecoratedTextEditor {
	protected QEditorOutlinePage outline=createOutlinePage();
	protected UtilListenableProperty<AbstractBuilder> builder;
	NoExceptionAutoClosable cl;
	NoExceptionAutoClosable closeit;
	private IFile file;
	private UtilEventListener<Set<IFile>> liRebuilt=e->{
		if(e==null || e.contains(file))
		{
			ResourceSet rs=getResourceSet();
			if(rs!=null)
			{
				ResourceCrossReferenceAdapter rcra=getResourceOfEditor();
				if(rcra!=null)
				{
					StyleBasedColoring sbc=rcra.getColoring();
					getSourceViewer().getTextWidget().getDisplay().asyncExec(()->{
						setColoring(sbc);
					});
				}
			}
		}
	};
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if(input instanceof IFileEditorInput)
		{
			IFileEditorInput fep=(IFileEditorInput) input;
			file=fep.getFile();
			IProject project=fep.getFile().getProject();
			builder=AbstractBuilder.getBuilderFor(getBuilderId(), project.getName());
		}else
		{
			builder=new UtilListenableProperty<>();
		}
		cl=builder.addListenerWithInitialTrigger(e->{
			if(closeit!=null)
			{
				closeit.close();
				closeit=null;
			}
			if(e!=null)
			{
				e.filesRebuilt.addListener(liRebuilt);
				closeit=new NoExceptionAutoClosable() {
					@Override public void close(){
						e.filesRebuilt.removeListener(liRebuilt);
					}
				};
			}
		});
		outline.setEditorInput(input);
		outline.setBuilder(builder);
	}
	private void setColoring(StyleBasedColoring sbc) {
		TextPresentation tp=getStyles().getTextParameters(sbc);
		((SourceViewer)getSourceViewer()).changeTextPresentation(tp, true);
	}
	abstract protected SwtStyleBasedColoring getStyles();
	@Override
	public void dispose() {
		cl.close();
		if(closeit!=null)
		{
			closeit.close();
			closeit=null;
		}
		super.dispose();
	}
	protected QEditorOutlinePage createOutlinePage() {
		return new QEditorOutlinePage();
	}
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		((StyledText)getAdapter(Control.class)).addCaretListener(e->{
			int position=e.caretOffset;
			updateSelection(e.caretOffset);
			getSelectionProvider().setSelection(new ISelection() {
					@Override
					public boolean isEmpty() {
						return true;
					}
					@Override
					public String toString() {
						// TODO finish selection feature
						return "HELLOBELLO "+position;
					}
				});
		});
	}
	private void updateSelection(int caretOffset) {
		List<CrossReferenceAdapter> possibles=new ArrayList<CrossReferenceAdapter>();
		ResourceSet rs=getResourceSet();
		if(rs!=null)
		{
			synchronized (rs) {
				ResourceCrossReferenceAdapter rcra=getResourceOfEditor();
				if(rcra!=null)
				{
					UtilVisitor.visitModel(rcra.getResource(), new UtilVisitor.Visitor(){
						@Override
						public Object visit(EObject element) {
							CrossReferenceAdapter cra=CrossReferenceAdapter.getAllowNull(element);
							if(cra!=null)
							{
								SourceReference sr=cra.getSourceReference();
								if(sr!=null)
								{
									if(sr.getTextIndexFrom()<=caretOffset && sr.getTextIndexTo()>caretOffset)
									{
										possibles.add(cra);
									}
								}
							}
							return null;
						}
					});
				}
			}
		}
		Collections.sort(possibles, new Comparator<CrossReferenceAdapter>() {
			@Override
			public int compare(CrossReferenceAdapter o1, CrossReferenceAdapter o2) {
				return o1.getSourceReference().getLength()-o2.getSourceReference().getLength();
			}
		});
		if(possibles.size()>0)
		{
			EObject eo=(EObject)possibles.get(0).getTarget();
			outline.setSelectedEmfObject(eo);
			QEditorSelectionSingleton.getInstance().selectionEvent.eventHappened(
					new Pair<IProject, EObject>(file.getProject(), eo));
		}
	}
	private ResourceSet getResourceSet() {
		AbstractBuilder current=builder.getProperty();
		if(current !=null && getEditorInput() instanceof IFileEditorInput)
		{
			IFileEditorInput fei=(IFileEditorInput) getEditorInput();
			String id=AbstractIncrementalBuilder.getFileIdentifier(fei.getFile());
			ResourceSet rs=current.buildResult.getProperty();
			return rs;
		}
		return null;
	}
	private ResourceCrossReferenceAdapter getResourceOfEditor() {
		AbstractBuilder current=builder.getProperty();
		if(current !=null && getEditorInput() instanceof IFileEditorInput)
		{
			IFileEditorInput fei=(IFileEditorInput) getEditorInput();
			String id=AbstractIncrementalBuilder.getFileIdentifier(fei.getFile());
			ResourceSet rs=current.buildResult.getProperty();
			List<CrossReferenceAdapter> possibles=new ArrayList<CrossReferenceAdapter>();
			if(rs!=null)
			{
				synchronized (rs) {
					ResourceCrossReferenceAdapter rcra=findResouceForEditor(rs, id);
					return rcra;
				}
			}
		}
		return null;
	}
	private ResourceCrossReferenceAdapter findResouceForEditor(ResourceSet rs, String id) {
		synchronized (rs) {
			for(Resource r: rs.getResources())
			{
				ResourceCrossReferenceAdapter rcra=ResourceCrossReferenceAdapter.get(r);
				if(rcra!=null)
				{
					if(rcra.getDoc().id.equals(id))
					{
						return rcra;
					}
				}
			}
		}
		return null;
	}
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		// System.out.println("Get adapter: "+adapter);
		if(adapter == IContentOutlinePage.class)
		{
			return outline;
		}
		return super.getAdapter(adapter);
	}
	/**
	 * Configure the builder id. Required so that the result of the builder can be connected to the editor
	 * that provides outline view and all decoration of the text.
	 * Builder is found by this id through {@link AbstractBuilder}.getBuilderFor(String builderId, String projectId)
	 * @return
	 */
	abstract public String getBuilderId();
}
