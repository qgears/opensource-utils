package hu.qgears.commons;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

public class MessageDigestOutputStream extends FilterOutputStream {
	MessageDigest md;
	public MessageDigestOutputStream(OutputStream out, MessageDigest md) {
		super(out);
		this.md=md;
	}
	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
		md.update(b);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		md.update(b, off, len);
	}
	@Override
	public void write(int b) throws IOException {
		out.write(b);
		md.update((byte)b);
	}
	public String getSha1()
	{
		return UtilSha1.toSha1String(md);
	}
}
