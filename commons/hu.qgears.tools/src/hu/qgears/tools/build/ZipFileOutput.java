package hu.qgears.tools.build;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import hu.qgears.tools.ReproducibleZipFile;

public class ZipFileOutput implements IFileOutput
{
	class EntryOutputStream extends OutputStream
	{
		private boolean closed=false;
		@Override
		public void close() throws IOException {
			if(!closed)
			{
				zos.closeEntry();
			}
			closed=true;
		}
		@Override
		public void write(int b) throws IOException {
			zos.write(b);
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			zos.write(b, off, len);
		}
		@Override
		public void write(byte[] b) throws IOException {
			zos.write(b);
		}
		@Override
		public void flush() throws IOException {
			zos.flush();
		}
	}
	ZipOutputStream zos;
	
	public ZipFileOutput(ZipOutputStream zos) {
		super();
		this.zos = zos;
	}

	@Override
	public OutputStream createOutputStream(String p, Date authorDate) throws IOException {
		zos.putNextEntry(ReproducibleZipFile.fix(new ZipEntry(p), authorDate.getTime()));
		return new EntryOutputStream();
	}

	@Override
	public void close() throws IOException {
		zos.close();
	}

	@Override
	public IFileOutput open() {
		return this;
	}
}
