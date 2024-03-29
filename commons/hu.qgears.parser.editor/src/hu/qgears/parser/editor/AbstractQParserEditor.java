package hu.qgears.parser.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyleRange;
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
import hu.qgears.emfcollab.backref.EmfReference;
import hu.qgears.emfcollab.util.UtilVisitor;
import hu.qgears.parser.coloring.StyleBasedColoring;
import hu.qgears.parser.contentassist.ICompletitionProposalContext;
import hu.qgears.parser.editor.coloring.SwtStyleBasedColoring;
import hu.qgears.parser.editor.textselection.TextSelection;
import hu.qgears.xtextgrammar.CRAEObject;
import hu.qgears.xtextgrammar.CRAEReference;
import hu.qgears.xtextgrammar.CRAResource;
import hu.qgears.xtextgrammar.SourceReference;

abstract public class AbstractQParserEditor extends AbstractDecoratedTextEditor {
	protected QEditorOutlinePage outline=createOutlinePage();
	protected UtilListenableProperty<AbstractBuilder> builder;
	NoExceptionAutoClosable cl;
	NoExceptionAutoClosable closeit;
	private IFile file;
	private Composite parentComposite;
	private UtilEventListener<Set<IFile>> liRebuilt=e->{
		if(e==null || e.contains(file))
		{
			ResourceSet rs=getResourceSet();
			if(rs!=null)
			{
				CRAResource rcra=getResourceOfEditor();
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
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(createSourceViewerConfiguration());
	}
	private SourceViewerConfiguration createSourceViewerConfiguration() {
		return new QPViewerConfiguration(this);
	}
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
		SourceViewer sv=((SourceViewer)getSourceViewer());
		int length=sv.getDocument().getLength();
		TextPresentation tp=getStyles().getTextParameters(sbc, length, sv.getDocument());
		try
		{
//			StyleRange defaultStyleRange=tp.getDefaultStyleRange();
//			if(defaultStyleRange!=null)
//			{
//				System.out.println("def range: "+defaultStyleRange.start+" l:"+defaultStyleRange.length);
//			}
//			Iterator<?> it=tp.getNonDefaultStyleRangeIterator();
			sv.changeTextPresentation(tp, true);
		}catch(Exception e)
		{
			e.printStackTrace();
			// TODO handle apply coloring exception
		}
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
		this.parentComposite=parent;
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
	public IHyperlink[] findLink(IRegion region)
	{
		Map<TextSelection, IHyperlink> ret=new HashMap<>();
		region.getOffset();

		List<TextSelection> possibles=new ArrayList<>();
		ResourceSet rs=getResourceSet();
		if(rs!=null)
		{
			synchronized (rs) {
				CRAResource rcra=getResourceOfEditor();
				if(rcra!=null)
				{
					UtilVisitor.visitModel(rcra.getResource(), new UtilVisitor.Visitor(){
						@Override
						public Object visit(EObject element) {
							CRAEObject cra=CRAEObject.getAllowNull(element);
							if(cra!=null)
							{
								for(CRAEReference cri: cra.getManagedReferences())
								{
									SourceReference sr=cri.getSourceReference();
									if(TextSelection.isCaretInside(sr, getResourceOfEditor().getDoc(), region.getOffset()))
									{
										System.out.println("Ref: "+cri.r.getName()+" "+sr.getLength());
										CRAEObject targetWrap=cri.getCurrentTarget().getProperty();
										if(cri.r!=null && targetWrap!=null && targetWrap.getTarget() instanceof EObject)
										{
											SourceReference srt=targetWrap.getSourceReference();
											if(srt!=null)
											{
												RefInTree ref=RefInTree.create(element, cri.r, (EObject)cri.targetA.getTarget(), cri.index);
												TextSelection ts=createTextSelection(ref, sr);
												possibles.add(ts);
												ret.put(ts, ResourceHyperlink.createForElement(sr, srt));
											}
										}
									}
								}
							}
							return null;
						}
					});
				}
			}
		}
		Collections.sort(possibles);
		if(possibles.size()>0)
		{
			IHyperlink hl=ret.get(possibles.get(0));
			return new IHyperlink[]{hl};
		}
		return null;
	}
	private class UpdateSelection implements Runnable
	{
		List<TextSelection> possibles=new ArrayList<>();
		private int caretOffset;
		private volatile boolean cancelled;
		public UpdateSelection(int caretOffset) {
			super();
			this.caretOffset = caretOffset;
		}

		@Override
		public void run() {
			ResourceSet rs=getResourceSet();
			if(rs!=null)
			{
				if(cancelled)
				{
					return;
				}
				synchronized (rs) {
					CRAResource rcra=getResourceOfEditor();
					if(rcra!=null)
					{
						if(cancelled)
						{
							return;
						}
						UtilVisitor.visitModel(rcra.getResource(), new UtilVisitor.Visitor(){
							@Override
							public Object visit(EObject element) {
								CRAEObject cra=CRAEObject.getAllowNull(element);
								if(cra!=null)
								{
									for(CRAEReference cri: cra.getManagedReferences())
									{
										SourceReference sr=cri.getSourceReference();
										if(TextSelection.isCaretInside(sr, rcra.getDoc(), caretOffset))
										{
											// System.out.println("Ref: "+cri.r.getName()+" "+sr.getLength());
											if(cri.r!=null && cri.targetA!=null && cri.targetA.getTarget() instanceof EObject)
											{
//												EmfBackReferenceImpl bri=EmfBackReferenceImpl.getByEobject(element);
//												if(bri!=null)
//												{
//													EObject target=(EObject)cri.targetA.getTarget();
//													Object tg1=element.eGet(cri.r);
//													System.out.println("Target1 and 2"+target+" "+tg1);
//													EmfReferenceImpl ref=bri.getSourceReference(element, cri.r, (EObject)tg1, 0);
//													if(ref!=null)
//													{
//														possibles.add(createTextSelection(ref, sr));
//													}
//												}
												RefInTree ref=RefInTree.create(element, cri.r, (EObject)cri.targetA.getTarget(), cri.index);
												possibles.add(createTextSelection(ref, sr));
											}
										}
									}
									SourceReference sr=cra.getSourceReference();
									if(TextSelection.isCaretInside(sr, rcra.getDoc(), caretOffset))
									{
										possibles.add(createTextSelection(sr, element));
									}
								}
								return null;
							}
						});
					}
				}
				if(cancelled)
				{
					return;
				}
				Collections.sort(possibles);
				if(possibles.size()>0)
				{
					parentComposite.getDisplay().asyncExec(()->{
						TextSelection ts=possibles.get(0);
						// outline.setSelection(ts);
						RefInTree ref=ts.getRef();
						EmfReference eref=ts.getERef();
						EObject eo=(EObject)ts.getTarget();
						if(eref!=null)
						{
							outline.setSelectedEmfRef(eref);
						}else if(ref!=null)
						{
							outline.setSelectedEmfRef(ref);
						}else if(eo!=null)
						{
							outline.setSelectedEmfObject(eo);
						}
						if(eo!=null)
						{
							QEditorSelectionSingleton.getInstance().selectionEvent.eventHappened(
									new Pair<IProject, EObject>(file.getProject(), eo));
						}
					});
				}
			}
//			System.out.println("List once: ");
//			for(TextSelection cra: possibles)
//			{
//				System.out.println("Text Selection: "+cra);
//			}
		}

		public void cancel() {
			cancelled=true;
		}
	}
	private UpdateSelection updateSelectionCallable;
	private void updateSelection(int caretOffset) {
		if(updateSelectionCallable!=null)
		{
			updateSelectionCallable.cancel();
			updateSelectionCallable=null;
		}
		updateSelectionCallable=new UpdateSelection(caretOffset);
		executeOnBuilderThread(updateSelectionCallable);
	}
	protected TextSelection createTextSelection(RefInTree ref, SourceReference sr) {
		return new TextSelection(ref, sr);
	}
	protected TextSelection createTextSelection(EmfReference ref, SourceReference sr) {
		return new TextSelection(ref, sr);
	}
	protected TextSelection createTextSelection(SourceReference sr, EObject eo) {
		return new TextSelection(eo, sr);
	}
//	protected TextSelection createTextSelection(CrossReferenceInstance cri) {
//		return new TextSelection(cri.);
//	}
	private boolean executeOnBuilderThread(Runnable r)
	{
		AbstractBuilder current=builder.getProperty();
		if(current !=null)
		{
			AbstractIncrementalBuilder incb=current.getIncrementalBuilder();
			return incb.executeOnBuilderThread(r);
		}
		return false;
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
	private CRAResource getResourceOfEditor() {
		AbstractBuilder current=builder.getProperty();
		if(current !=null && getEditorInput() instanceof IFileEditorInput)
		{
			IFileEditorInput fei=(IFileEditorInput) getEditorInput();
			String id=AbstractIncrementalBuilder.getFileIdentifier(fei.getFile());
			ResourceSet rs=current.buildResult.getProperty();
			List<CRAEObject> possibles=new ArrayList<CRAEObject>();
			if(rs!=null)
			{
				synchronized (rs) {
					CRAResource rcra=findResouceForEditor(rs, id);
					return rcra;
				}
			}
		}
		return null;
	}
	private CRAResource findResouceForEditor(ResourceSet rs, String id) {
		synchronized (rs) {
			for(Resource r: rs.getResources())
			{
				CRAResource rcra=CRAResource.get(r);
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
	abstract public ICompletitionProposalContext getProposalContext();
	public UtilListenableProperty<AbstractBuilder> getBuilder() {
		return builder;
	}
}
