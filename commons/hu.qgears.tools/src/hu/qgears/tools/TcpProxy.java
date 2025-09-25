package hu.qgears.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.Slot;
import joptsimple.annot.JOHelp;
import joptsimple.tool.AbstractTool;

public class TcpProxy extends AbstractTool
{
	private long counter;
	public class Args implements IArgs
	{
		@JOHelp("logging folder")
		public File log;
		@JOHelp("Host to open server port on.")
		public String serverHost;
		@JOHelp("Port to open server port on.")
		public int serverPort;
		@JOHelp("Host to connect to.")
		public String connectHost;
		@JOHelp("Port to connect to.")
		public int connectPort;
		@JOHelp("Automatically flush datastream after each transferred block.")
		public boolean autoFlush;
		@Override
		public void validate() {
			if(serverHost==null||serverPort==0)
			{
				throw new IllegalArgumentException("serverHost and serverPort has to be specified.");
			}
			if(connectHost==null||connectPort==0)
			{
				throw new IllegalArgumentException("connectHost and connectPort has to be specified.");
			}
			if(log==null)
			{
				throw new IllegalArgumentException("log folder must be specified.");
			}
		}
	}
	
	private class Closer implements Slot<SignalFuture<Object>>
	{
		Socket[] s;
		
		public Closer(Socket[] s) {
			super();
			this.s = s;
		}

		@Override
		public void signal(SignalFuture<Object> value) {
			for(Socket ss:s)
			{
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private class ServeConnection
	{
		Args a;
		Socket s;
		public ServeConnection(Args a, Socket s) {
			super();
			this.a = a;
			this.s = s;
		}
		public void start() {
			try {
				final InputStream is=s.getInputStream();
				final OutputStream os=s.getOutputStream();
				Socket connectSocket=new Socket(a.connectHost, a.connectPort);
				Closer c=new Closer(new Socket[]{connectSocket, s});
				File folder=new File(a.log, ""+System.currentTimeMillis()+"_"+(counter++));
				folder.mkdirs();
				new StreamTeeConnector()
					.setAutoFlush(a.autoFlush)
					.start(is, new OutputStream[]{connectSocket.getOutputStream(),
						new FileOutputStream(new File(folder, "request"))}).addOnReadyHandler(c);
				new StreamTeeConnector()
					.setAutoFlush(a.autoFlush)
					.start(connectSocket.getInputStream(), new OutputStream[]{os,
						new FileOutputStream(new File(folder, "response"))}).addOnReadyHandler(c);;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					s.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	@Override
	public String getId() {
		return "tcpProxy";
	}

	@Override
	public String getDescription() {
		return "TCP proxy with logging capability";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		Args args=(Args)a;
		ServerSocket ss=new ServerSocket();
		try
		{
			ss.bind(new InetSocketAddress(args.serverHost, args.serverPort));
			while(true)
			{
				Socket s=ss.accept();
				new ServeConnection(args, s).start();
			}
		}finally
		{
			ss.close();
		}
	}

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
}
