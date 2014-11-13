package hu.qgears.emfcollab.util;

import hu.qgears.emfcollab.impl.LoadedResource;
import hu.qgears.emfcollab.load.UtilVisitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;


public class UtilEmfModelIO {
	/**
	 * Add an XMI id to each element (that does not have one already) so that
	 * model cross references are by ID, not by XML tree position.
	 * 
	 * @param notationModel
	 */
	public static void addIds(final LoadedResource loadedResource) {
		for (EObject obj : loadedResource.getResource().getContents()) {
			UtilVisitor.visitModel(obj, new UtilVisitor.Visitor() {
				@Override
				public Object visit(EObject element) {
					loadedResource.getIdSoruce().getId(loadedResource.getResource(), element);
					return null;
				}
			});
		}
	}
	/**
	 * Serialise the EMF model to an in-memory XML. 
	 * @param resource EMF resource to be serialized.
	 * @param progressMonitor
	 * @return The serialised XML file in a memory byte array. 
	 * @throws IOException
	 */
	public static byte[] saveModelToMemory(
			LoadedResource loadedResource) throws IOException {
		addIds(loadedResource);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		loadedResource.getResource().save(bos, getSaveOptions());
		return bos.toByteArray();
	}
	/**
	 * Save the XMI resource to file.
	 * 
	 * @param resource
	 *            the resource instance
	 * @throws IOException
	 */
	public static void saveModel(LoadedResource resource) throws IOException {
		addIds(resource);
		resource.getResource().save(getSaveOptions());
	}
	/**
	 * Save the XMI resource to file.
	 * 
	 * @param resource
	 *            the resource instance
	 * @param f
	 *            new file to save resource to
	 * @param progressMonitor
	 * @throws IOException
	 */
	public static void saveModel(LoadedResource resource, File f) throws IOException {
		FileOutputStream fos=new FileOutputStream(f);
		try
		{
			addIds(resource);
			resource.getResource().save(fos, getSaveOptions());
		}finally
		{
			fos.close();
		}
	}
	/**
	 * Get model save options.
	 * 
	 * @return
	 */
	private static Map<?, ?> getSaveOptions() {
		return new HashMap<Object, Object>();
	}
	/**
	 * Get default resource load options.
	 * 
	 * Boost model load using XMLResource.OPTION_DEFER_IDREF_RESOLUTION
	 * 
	 * @return
	 */
	public static Map<?, ?> getLoadOptions() {
		Map<Object, Object> ret=new HashMap<Object,Object>();
		// Boost model load.
		ret.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
		return ret;
	}
	/**
	 * Do all necessary registration to use
	 * EMF in an OSGI environment.
	 * @param ecoreFiles some EMF metamodels can be defined just by the ecore file
	 * @param packages in case of GMF the metamodel must be defined using the special code. This parameter makes sur that is loaded. Eg: NotationPackage.eINSTANCE
	 * @throws IOException 
	 */
	public static void registerAllNecessary(List<File> ecoreFiles,
			List<EPackage> packages) throws IOException
	{
		// Register the default resource factory -- only needed for
		// stand-alone!
//		Resource.Factory.Registry resFactReg=Resource.Factory.Registry.INSTANCE;
//		resFactReg.getExtensionToFactoryMap().put(
//				Resource.Factory.Registry.DEFAULT_EXTENSION,
//				new UUIDXmiResourceFactoryImpl());
		// Initialize ecore package.
		org.eclipse.emf.ecore.EPackage.Registry registry=org.eclipse.emf.ecore.EPackage.Registry.INSTANCE;
		EcorePackage theCorePackage = EcorePackage.eINSTANCE;
		register(registry, theCorePackage);
		ResourceSet resourceSet = new ResourceSetImpl();
		for(File f: ecoreFiles)
		{
			registerEcoreFile(resourceSet, registry, f);
		}
	}
//	public static void registerFileExtension()
	/**
	 * Register ecore file. The ecore file generates
	 * an automatic (de)serialization method
	 * and reflective object creation.
	 * @param resourceSet
	 * @param registry
	 * @param f
	 * @throws IOException
	 */
	public static void registerEcoreFile(
			ResourceSet resourceSet,
			final org.eclipse.emf.ecore.EPackage.Registry registry,
			File f) throws IOException
	{
		Resource res=new UUIDXmiResource(URI.createURI(
				f.toURI().toString()));
		res.load(UtilEmfModelIO.getLoadOptions());
		List<EObject> l=res.getContents();
		for(EObject o: l)
		{
			UtilVisitor.visitModel(o, new UtilVisitor.Visitor(){
				@Override
				public Object visit(EObject element) {
					if(element instanceof EPackage)
					{
						EPackage pack=(EPackage) element;
						register(registry, pack);
					}
					return null;
				}
			});
		}
	}
	public static void register(Registry registry, EPackage pack) {
		registry.put(pack.getNsURI(), pack);
	}
	public static Resource loadFile(File g) throws IOException {
		ResourceSet resourceSet=new ResourceSetImpl();
		UUIDXmiResource res=new UUIDXmiResource();
		res.setURI(URI.createFileURI(g.getAbsolutePath()));
		resourceSet.getResources().add(res);
		res.load(getLoadOptions());
		return res;
	}
	/**
	 * Load an EMF resource.
	 * 
	 * @param uriString the uri of the resource to be loaded.
	 * @param content
	 * @param resourceSet 
	 * @return the resource loaded
	 * @throws CoreException
	 * @throws IOException
	 */
	public static XMIResource loadFile(
			String uriString,
			byte[] content,
			ResourceSet resourceSet)
			throws Exception {
		URI uri = URI.createPlatformResourceURI(uriString,
				true);
		UUIDXmiResource resource=new UUIDXmiResource();
		resource.setURI(uri);
		resourceSet.getResources().add(resource);

		if (!resource.isLoaded()) {
			try {
				resource.load(new ByteArrayInputStream(content),
						getLoadOptions());
			} catch (IOException e) {
				resource.unload();
				throw e;
			}
		}
		return (XMIResource) resource;
	}
}
