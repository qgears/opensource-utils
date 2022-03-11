package hu.qgears.tools.build.gen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import hu.qgears.commons.UtilFile;

public class FileContent {
	String id;
	private File ownerJar;
	private File dir;
	private byte[] data;
	public FileContent(String id, File dir) {
		this.id=id;
		this.dir=dir;
	}

	public FileContent(String id, File ownerJar, ByteArrayOutputStream bos) {
		this.id=id;
		this.data=bos.toByteArray();
		this.ownerJar=ownerJar;
	}
	public FileContent(String id, byte[] data) {
		this.id=id;
		this.data=data;
	}
	
	@Override
	public String toString() {
		if(dir!=null)
		{
			return id+" "+dir;
		}else
		{
			return id+" "+ownerJar;
		}
	}

	public void streamConentTo(OutputStream os) throws IOException {
		if(data!=null)
		{
			os.write(data);
		}else
		{
			try(FileInputStream fis=new FileInputStream(dir))
			{
				byte[] buffer = new byte[UtilFile.defaultBufferSize.get()];
				int n;
				while ((n = fis.read(buffer)) > 0) {
					os.write(buffer, 0, n);
				}
			}
		}
	}
}
