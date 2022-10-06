package hu.qgears.parser.editor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import hu.qgears.xtextgrammar.SourceReference;

public class ResourceHyperlink implements IHyperlink {

    private IRegion region;
    private String hyperlinkText;
    private IFile resource;
    private IRegion tgReg;

    public ResourceHyperlink(IRegion region, String hyperlinkText, IFile resource, IRegion tgReg) {
        this.region = region;
        this.hyperlinkText = hyperlinkText;
        this.resource = resource;
        this.tgReg=tgReg;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return hyperlinkText;
    }

    @Override
    public void open() {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            IEditorPart ep=IDE.openEditor(activePage, resource);
            if(ep instanceof ITextEditor)
            {
            	ITextEditor editor = (ITextEditor) ep;
                editor.selectAndReveal(tgReg.getOffset(), tgReg.getLength());
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

	public static ResourceHyperlink createForElement(SourceReference src, SourceReference tg) {
//		CircuitElement target, Region srcReg
//		String id=""+target;
//		if(target instanceof CircuitElementFqn)
//		{
//			id=UtilString.concat(((CircuitElementFqn) target).getFqId(),".");
//		}
		String fileid=tg.getDoc().id;
		IFile g=ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileid));
		System.out.println("target position in file: "+tg.getTextIndexFrom()+" "+fileid);
		Region tgReg=new Region(tg.getTextIndexFrom(), tg.getLength());
		Region srcReg=new Region(src.getTextIndexFrom(),  src.getLength());
		return new ResourceHyperlink(srcReg, "myId", g, tgReg);
	}
}