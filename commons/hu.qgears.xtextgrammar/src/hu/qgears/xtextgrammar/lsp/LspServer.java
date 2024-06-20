package hu.qgears.xtextgrammar.lsp;

import java.net.ServerSocket;
import java.net.Socket;

public class LspServer {
	ILspServerModel model;
	public LspServer(ILspServerModel model) {
		this.model=model;
	}
	public void run() throws Exception {
		try(ServerSocket ss=new ServerSocket(5007))
		{
			while(true)
			{
				Socket s=ss.accept();
				new LspServerSession(s).start();
			}
		}
	}
}
