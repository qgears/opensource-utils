package hu.qgears.parser.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.ResourceSet;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilListenableProperty;

abstract public class AbstractBuilder extends IncrementalProjectBuilder {
	private AbstractIncrementalBuilder incBuilder;
	private Set<IFile> changed=new HashSet<>();
	private boolean initialized=false;
	private static Map<String, Map<String,UtilListenableProperty<AbstractBuilder>>> builders=new HashMap<>();
	/**
	 * Resource set that is the result of the build.
	 * This may be changed on the builder thread so clients have to implement
	 * thread safe access to it.
	 * TODO how to implement thread safe access?
	 */
	public final UtilListenableProperty<ResourceSet> buildResult=new UtilListenableProperty<>();
	public final UtilEvent<ResourceSet> modelUpdated=new UtilEvent<>();
	public final UtilEvent<Set<IFile>> filesRebuilt=new UtilEvent<>();
	/**
	 * Get the builder as a listenable property.
	 * In case the builder is not initialized yet for the type and project combination then
	 * the property value will be null and can be listened to be initialized later.
	 * @param builderId
	 * @param projectId
	 * @return
	 */
	synchronized public static UtilListenableProperty<AbstractBuilder> getBuilderFor(String builderId, String projectId)
	{
		Map<String, UtilListenableProperty<AbstractBuilder>> byProject=builders.get(builderId);
		if(byProject==null)
		{
			byProject=new HashMap<>();
			builders.put(builderId, byProject);
		}
		UtilListenableProperty<AbstractBuilder> ret=byProject.get(projectId);
		if(ret==null)
		{
			ret=new UtilListenableProperty<>();
			byProject.put(projectId, ret);
		}
		return ret;
	}
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
		if(getProject()!=null)
		{
			String projectId=getProject().getName();
			UtilListenableProperty<AbstractBuilder> bldprop=getBuilderFor(getBuilderId(), projectId);
			bldprop.setProperty(this);
		}
		if (kind == FULL_BUILD||!initialized) {
			initialized=true;
			incBuilder.fullBuild(getProject(), monitor, ResourcesPlugin.getWorkspace());
			filesRebuilt.eventHappened(null);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				incBuilder.fullBuild(getProject(), monitor, ResourcesPlugin.getWorkspace());
				filesRebuilt.eventHappened(null);
			} else {
				changed=new HashSet<>();
				delta.accept(new SampleDeltaVisitor());
				incBuilder.incrementalBuild(getProject(), changed, monitor);
				filesRebuilt.eventHappened(changed);
				changed=null;
			}
		}
		modelUpdated.eventHappened(buildResult.getProperty());
		return null;
	}
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(getMarkerType(), true, IResource.DEPTH_INFINITE);
	}
	abstract protected String getMarkerType();
	private IResourceChangeListener rcl;
	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
		IProject p=getProject();
		String projectId=p.getName();
		rcl=new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				switch(event.getType())
				{
				case IResourceChangeEvent.PRE_CLOSE:
				case IResourceChangeEvent.PRE_DELETE:
					if(event.getResource() instanceof IProject)
					{
						IProject todelete=(IProject) event.getResource();
						if(projectId.equals(todelete.getName()))
						{
							p.getWorkspace().removeResourceChangeListener(rcl);
							disposeBuilder(projectId);
						}
					}
				}
			}
		};
		getProject().getWorkspace().addResourceChangeListener(rcl);
	}
	protected void disposeBuilder(String projectId) {
		UtilListenableProperty<AbstractBuilder> bld=getBuilderFor(getBuilderId(), projectId);
		bld.setProperty(null);
		incBuilder.dispose();
	}
	/**
	 * Identifier of the builder.
	 * Used to store builder result in a globally accessible cache.
	 * @return
	 */
	abstract protected String getBuilderId();
}
