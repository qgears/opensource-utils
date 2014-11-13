package hu.qgears.nativeloader;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


public class StartNativeProcess {
	Process p;
	OutputStream out=System.out;
	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public OutputStream getErr() {
		return err;
	}

	public void setErr(OutputStream err) {
		this.err = err;
	}
	OutputStream err=System.err;
	/**
	 * Start native process copied from classloader to the
	 * filesystem and starting the binary code
	 * @param cl
	 * @param processName
	 * @param param
	 */
	public void start(Class<?> cl, String processName, String param) {
		try {
			String fileName=processName;
			if(System.getProperty("os.name").toUpperCase().startsWith("WIN"))
			{
				fileName=fileName+".exe";
			}
			byte[] nc=UtilFile.loadFile(cl.getResource(fileName));
			File dir=UtilNativeLoader.getDirectory();
			File client=new File(dir, fileName);
			UtilFile.checkSaveAsFile(client, nc);
			client.setExecutable(true);
			if(param!=null)
			{
				p=Runtime.getRuntime().exec(
					new String[]{client.getAbsolutePath(), param});
			}else
			{
				p=Runtime.getRuntime().exec(
						new String[]{client.getAbsolutePath()});
			}
			if(out!=null)
			{
				csOut=new ConnectStreams(p.getInputStream(), out);
				csOut.setCloseOs(false);
				csOut.start();
			}
			if(err!=null)
			{
				csErr=new ConnectStreams(p.getErrorStream(), err);
				csErr.setCloseOs(false);
				csErr.start();
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Start native process that is already installed on the box
	 * @param cl
	 * @param processName
	 * @param param
	 */
	public void start(String filename, String param) throws IOException{
		try {
			File client=new File(filename);
			if(param!=null)
			{
				p=Runtime.getRuntime().exec(
					new String[]{client.getAbsolutePath(), param});
			}else
			{
				p=Runtime.getRuntime().exec(
						new String[]{client.getAbsolutePath()});
			}
			if(out!=null)
			{
				csOut=new ConnectStreams(p.getInputStream(), out);
				csOut.setCloseOs(false);
				csOut.start();
			}
			if(err!=null)
			{
				csErr=new ConnectStreams(p.getErrorStream(), err);
				csErr.setCloseOs(false);
				csErr.start();
			}
		} catch (Throwable e) {
			throw new IOException("Starting process "+filename, e);
		}
	}
	ConnectStreams csOut;
	ConnectStreams csErr;

	public void stop() {
		if(p!=null)
		{
			p.destroy();
			p=null;
		}
		if(csOut!=null)
		{
			csOut.close();
			csOut=null;
		}
		if(csErr!=null)
		{
			csErr.close();
			csErr=null;
		}
	}

	public Process getProcess() {
		return p;
	}
}
