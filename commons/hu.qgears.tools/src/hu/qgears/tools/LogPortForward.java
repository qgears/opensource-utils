package hu.qgears.tools;

import java.net.ServerSocket;
import java.net.Socket;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.StreamTee;
import joptsimple.tool.AbstractTool;

public class LogPortForward extends AbstractTool
{
	class Args implements IArgs
	{
		@Override
		public void validate() {
		}
	}
	
	static class Handle extends Thread
	{
		private Socket s;
		public Handle(Socket s) {
			this.s=s;
		}
		@Override
		public void run() {
			try {
				try (Socket c= new Socket("localhost", 8090)){
					Thread th1=ConnectStreams.startStreamThread(s.getInputStream(), 
							new StreamTee(c.getOutputStream(), true, System.out, false));
					Thread th2=ConnectStreams.startStreamThread(c.getInputStream(), 
							new StreamTee(s.getOutputStream(), true, System.err, false));
					th1.join();
					th2.join();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	@Override
	public String getId() {
		return "logPortforward";
	}
	@Override
	public String getDescription() {
		return "Do not use!";
	}
	@Override
	protected int doExec(IArgs a) throws Exception {
		try(ServerSocket ss=new ServerSocket(9000))
		{
			while(true)
			{
				Socket s=ss.accept();
				new Handle(s).start();
			}
		}
	}
	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
}
