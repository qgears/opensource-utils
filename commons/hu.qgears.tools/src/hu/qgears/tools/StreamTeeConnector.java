package hu.qgears.tools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.signal.SignalFutureWrapper;

public class StreamTeeConnector {
	public SignalFutureWrapper<Object> start(final InputStream is, final OutputStream[] oss)
	{
		final SignalFutureWrapper<Object> ret=new SignalFutureWrapper<>();
		new Thread()
		{
			public void run() {
				try {
					connect(is, oss);
					ret.ready("", null);
				} catch (IOException e) {
					e.printStackTrace();
					ret.ready(null, e);
				}
			};
		}
		.start();
		return ret;
	}
	public void connect(InputStream is, OutputStream[] oss) throws IOException
	{
		byte[] buffer=new byte[UtilFile.defaultBufferSize.get()];
		int n=0;
		try
		{
			while(n>=0)
			{
				n=is.read(buffer);
				if(n>0)
				{
					for(OutputStream os: oss)
					{
						os.write(buffer, 0, n);
					}
				}
			}
		}finally
		{
			closeSafe(is);
			for(OutputStream os: oss)
			{
				closeSafe(os);
			}
		}
	}

	private void closeSafe(Closeable is) {
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
