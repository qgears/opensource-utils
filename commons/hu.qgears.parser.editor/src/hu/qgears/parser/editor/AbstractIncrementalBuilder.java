package hu.qgears.parser.editor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * A single instance is maintained for each project.
 */
abstract public class AbstractIncrementalBuilder {
	public class Visitor implements IResourceVisitor
	{
		private List<IFile> collected=new ArrayList<>();
		@Override
		public boolean visit(IResource resource) throws CoreException {
			if(resource instanceof IFile)
			{
				IFile f=(IFile) resource;
				if(f.getName().endsWith(".dslgui") || f.getName().endsWith(".sdl") ||f.getName().endsWith(".fsm"))
				{
					collected.add(f);
				}
				return true;
			}
			return true;
		}	
	}
	public SignalFutureWrapper<Void> processFile(IFile file) {
		deleteMarkers(file);
		try {
			try(InputStream is=file.getContents())
			{
				String source=UtilFile.loadAsString(is);
				return processFile(file, source);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	abstract protected SignalFutureWrapper<Void> processFile(IFile file, String source);
	public void fullBuild(IProject project, IProgressMonitor monitor, IWorkspace workspace) {
		beforeFullBuild(project, monitor);
		try {
			Visitor v=new Visitor();
			project.accept(v);
			monitor.beginTask("DSL Build all", v.collected.size()*2);
			for(IFile f: v.collected)
			{
				SignalFutureWrapper<Void> ready=processFile(f);
				if(ready!=null)
				{
					ready.addOnReadyHandler(e->{monitor.worked(1);});
				}else
				{
					monitor.worked(1);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			try {
				afterFullBuild(project, monitor);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		monitor.done();
	}
	abstract protected void afterFullBuild(IProject project, IProgressMonitor monitor) throws Exception;
	/**
	 * Task before doing a full build: normally delete all results of previous builds.
	 * @param monitor 
	 * @param project 
	 */
	abstract protected void beforeFullBuild(IProject project, IProgressMonitor monitor);
	public void incrementalBuild(IProject iProject, Set<IFile> changed, IProgressMonitor monitor) {
		System.out.println("Delta build: "+changed);
		beforeIncrementalBuild(iProject, changed, monitor);
		for(IFile f: changed)
		{
			try {
				processFile(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			afterIncrementalBuild(iProject, changed, monitor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	abstract protected void afterIncrementalBuild(IProject iProject, Set<IFile> changed, IProgressMonitor monitor) throws Exception;
	abstract protected void beforeIncrementalBuild(IProject iProject, Set<IFile> changed, IProgressMonitor monitor);
	protected IMarker addMarker(IFile file, String message, int lineNumber, int from, int to,
			int severity) {
		try {
			IMarker m=file.createMarker(getMarkerType());
			m.setAttribute(IMarker.SEVERITY, severity);
			m.setAttribute(IMarker.MESSAGE, message);
			if(lineNumber>=0)
			{
				m.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}
			m.setAttribute(IMarker.CHAR_START, from);
			m.setAttribute(IMarker.CHAR_END, to);
			return m;
		} catch (CoreException e) {
		}
		return null;
	}
	abstract protected String getMarkerType();
	protected void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(getMarkerType(), false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	public static String getFileIdentifier(IFile file) {
		return file.getFullPath().toPortableString();
	}
}