package hu.qgears.parser.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

abstract public class AbstractBuilder extends IncrementalProjectBuilder {
	private AbstractIncrementalBuilder incBuilder;
	private Set<IFile> changed=new HashSet<>();
	private boolean initialized=false;
	public AbstractBuilder() {
		incBuilder=createBuilder();
	}
	abstract protected AbstractIncrementalBuilder createBuilder();
	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if(resource instanceof IFile)
			{
				changed.add((IFile) resource);
			}
			//return true to continue visiting children.
			return true;
		}
	}
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD||!initialized) {
			initialized=true;
			incBuilder.fullBuild(getProject(), monitor, ResourcesPlugin.getWorkspace());
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				incBuilder.fullBuild(getProject(), monitor, ResourcesPlugin.getWorkspace());
			} else {
				changed=new HashSet<>();
				delta.accept(new SampleDeltaVisitor());
				incBuilder.incrementalBuild(getProject(), changed);
				changed=null;
			}
		}
		return null;
	}
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(getMarkerType(), true, IResource.DEPTH_INFINITE);
	}
	abstract protected String getMarkerType();
	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
	}
}
