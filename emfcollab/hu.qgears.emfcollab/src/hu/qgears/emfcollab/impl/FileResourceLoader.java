package hu.qgears.emfcollab.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilSerializator;
import hu.qgears.commons.UtilTime;
import hu.qgears.emfcollab.IdSource;
import hu.qgears.emfcollab.XmiIdSource;
import hu.qgears.emfcollab.srv.EmfCredentials;
import hu.qgears.emfcollab.util.UtilEmfModelIO;

abstract public class FileResourceLoader implements IResourceLoader {
	private File dir;
	private IdSource idSource;

	/**
	 * Create a new file resource loader with a default
	 * XmiIdSource
	 * @param dir
	 */
	public FileResourceLoader(File dir) {
		super();
		this.dir = dir;
		this.idSource=new XmiIdSource();
	}
	public FileResourceLoader(File dir, IdSource idSource) {
		super();
		this.dir = dir;
		this.idSource=idSource;
	}

	@Override
	public ResourceWithHistory loadResource(EmfCredentials credentials, String resourceName) throws IOException {
		File g=new File(dir, resourceName);
		if(!g.exists())
		{
			throw new FileNotFoundException("'"+g.getAbsolutePath()+"'");
		}
		if(!g.canRead())
		{
			throw new FileNotFoundException("File can not be read by user: "+g.getAbsolutePath());
		}
		if(!g.canWrite())
		{
			throw new FileNotFoundException("File can not be written by user: "+g.getAbsolutePath());
		}
		Resource res=UtilEmfModelIO.loadFile(g);
		LoadedResource r=new LoadedResource(res, idSource);
		File historyf=getHistoryFile(resourceName);
		ResourceHistory history=loadHistory(historyf);
		//  TODO reload history!
		return new ResourceWithHistory(r, history);
	}
	private ResourceHistory loadHistory(File historyf) throws IOException {
		if(!historyf.exists())
		{
			return null;
		}else
		{
			byte[] bs=UtilFile.loadFile(historyf);
			ResourceHistory h;
			try {
				h = (ResourceHistory)UtilSerializator.deserialize(bs, getClass().getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
			return h;
		}
	}
	private File getHistoryFile(String resourceName)
	{
		File history=new File(dir, resourceName+".history");
		return history;
	}
	@Override
	public File getLogFile(String resourceName)
	{
		File history=new File(dir, resourceName+".log");
		return history;
	}
	@Override
	public void saveResource(EmfCredentials credentials,
			String resourceName,
			ResourceWithHistory resource,
			String commitLog) throws IOException {
		File outFile=new File(dir, "snapshots");
		outFile.mkdirs();
		outFile=new File(outFile, resourceName+"_"+UtilTime.createUserReadableTimeStamp());
		outFile.getParentFile().mkdirs();
		UtilEmfModelIO.saveModel(resource.getResource(), outFile);
		File history=getHistoryFile(resourceName);
		byte[] bs=UtilSerializator.serialize(resource.getHistory());
		UtilFile.saveAsFile(history, bs);
	}
	@Override
	public void commitResource(EmfCredentials credentials, String resourceName,
			ResourceWithHistory resource, String commitLog) throws IOException {
		UtilEmfModelIO.saveModel(resource.getResource());
		File history=getHistoryFile(resourceName);
		history.delete();
//		File log=getLogFile(resourceName);
//		FileOutputStream fos=new FileOutputStream(log, true);
//		try
//		{
//			OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
//			try {
//				new Serializate(osw).serializate(resource.getUndoList());
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			osw.close();
//		}finally
//		{
//			fos.close();
//		}
//		byte[] bs=UtilSerializator.serialize(resource.getHistory());
//		UtilFile.saveAsFile(history, bs);
	}
	@Override
	public void revertResource(String resourceName) {
		// nothing to do in not SVN case.
		
	}
}
