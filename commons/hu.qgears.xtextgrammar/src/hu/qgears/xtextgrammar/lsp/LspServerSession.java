package hu.qgears.xtextgrammar.lsp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

/**
 * Handle TCP connection of an LSP server communication session.
 */
public class LspServerSession extends Thread {
	Socket s;
	public static final int MAX_LINE_LENGTH=1024;

	public LspServerSession(Socket s) {
		super("LspServerSession");
		this.s = s;
	}

	InputStream is;

	@Override
	public void run() {
		try {
			is = s.getInputStream();
			while (true) {
				processInput();
				// System.out.print((char)ch);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String readLine() throws IOException {
		StringBuilder line = new StringBuilder();
		while (true) {
			int ch = is.read();
			if (ch < 0) {
				throw new EOFException();
			}
			switch (ch) {
			case '\r':
				break;
			case '\n':
				System.out.println("LINE: " + line);
				String l = line.toString();
				return l;
			default:
				line.append((char) ch);
				if(line.length()>MAX_LINE_LENGTH)
				{
					throw new IOException("Too long line");
				}
				break;
			}
		}
	}

	private void processInput() throws IOException {
		String l=readLine();
		if (l.startsWith("Content-Length: ")) {
				int length = Integer.parseInt(l.substring("Content-Length: ".length()));
				readLine();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				for (int i = 0; i < length; ++i) {
					int b = is.read();
					if (b < 0) {
						throw new IOException();
					}
					bos.write((byte) b);
				}
				String jsons = new String(bos.toByteArray(), StandardCharsets.UTF_8);
				System.out.println("JSON command: " + new JSONObject(jsons).toString(1));
				JSONObject ret=new JSONObject("{'status':'ok'}");
				byte[] retData=ret.toString().getBytes(StandardCharsets.UTF_8);
				s.getOutputStream().write(("Content-Length: "+retData.length+"\n\n").getBytes(StandardCharsets.UTF_8));
				s.getOutputStream().write(retData);
				s.getOutputStream().flush();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}else
		{
			throw new IOException("Unknown input: "+l);
		}
	}
}
