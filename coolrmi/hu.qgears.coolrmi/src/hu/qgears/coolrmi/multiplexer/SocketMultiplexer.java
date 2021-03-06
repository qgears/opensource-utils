package hu.qgears.coolrmi.multiplexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;



/**
 * Multiplexes several messages on a single TCP
 * connection to both direction.
 * 
 * Both write and read has an own thread.
 * 
 * @author rizsi
 *
 */
public class SocketMultiplexer implements ISocketMultiplexer{
	private boolean guaranteeOrdering;
	private ISocketMultiplexerListener messageListener;
	private InputStream is;
	private OutputStream os;
	private SocketMultiplexerDatagramStreamer streamer=new SocketMultiplexerDatagramStreamer();
	private String requiredHelloString;
	private String sendHelloString;
	private boolean exit=false;
	/**
	 * True means that the next messages can not be sent.
	 */
	private boolean disconnected;
	private long counter=0;
	private LinkedList<SocketMultiplexerSource> messagesToSend=new LinkedList<SocketMultiplexerSource>();
	public SocketMultiplexer(InputStream is, OutputStream os,
			ISocketMultiplexerListener messageListener,
			boolean guaranteeOrdering, boolean isClient) throws IOException {
		super();
		this.is = is;
		this.messageListener=messageListener;
		this.os=os;
		requiredHelloString="CoolRMI 2.0.0"+(isClient?" server":" client");
		sendHelloString="CoolRMI 2.0.0"+(!isClient?" server":" client");
	}
	public void start()
	{
		new ReadThread().start();
		new WriteThread().start();
	}
	public final static  int datagramMaxSize=2048;
	class ReadThread extends Thread
	{
		public ReadThread() {
			super("Cool RMI read thread");
		}
		Map<Long, ByteArrayOutputStream> messages=new HashMap<Long, ByteArrayOutputStream>();
		@Override
		public void run() {
			try {
				try
				{
					try {
						byte[] pattern=requiredHelloString.getBytes(StandardCharsets.UTF_8);
						byte[] recv=new byte[pattern.length];
						SocketMultiplexerDatagramStreamer.readAll(is, recv);
						if(!Arrays.equals(pattern, recv))
						{
							throw new IOException("Connection version string does not match! "+new String(recv, StandardCharsets.UTF_8)+" expected: "+requiredHelloString);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						exit=true;
					}
					while(!exit)
					{
						SocketMultiplexerDatagram datagram=streamer.readFromStream(is, datagramMaxSize);
						long id=datagram.getDatagramId();
						ByteArrayOutputStream bos=getMessage(id);
						bos.write(datagram.getContent());
						if(datagram.isLastPiece())
						{
							removeMessage(id);
							messageListener.messageReceived(bos.toByteArray());
						}
					}
				}finally
				{
					is.close();
				}
			} catch (Exception e) {
				messageListener.pipeBroken(e);
			}
		}
		private void removeMessage(long id) {
			messages.remove(id);
		}
		ByteArrayOutputStream getMessage(long id)
		{
			ByteArrayOutputStream bos=messages.get(id);
			if(bos==null)
			{
				bos=new ByteArrayOutputStream();
				messages.put(id, bos);
			}
			return bos;
		}
	}
	class WriteThread extends Thread
	{
		public WriteThread() {
			super("Cool RMI write thread");
		}
		int lastSent=-1;
		@Override
		public void run() {
			try {
				os.write(sendHelloString.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e1) {
				e1.printStackTrace();
				exit=true;
			}
			while(!exit)
			{
				SocketMultiplexerSource source;
				int toSendIndex;
				if(guaranteeOrdering)
				{
					toSendIndex=0;
				}else
				{
					toSendIndex=lastSent+1;
				}
				synchronized (messagesToSend) {
					if(toSendIndex>=messagesToSend.size())
					{
						toSendIndex=0;
					}
					if(messagesToSend.isEmpty())
					{
						try {
							messagesToSend.wait();
						} catch (InterruptedException e) {}
					}
					if(messagesToSend.isEmpty())
					{
						source=null;
					}else
					{
						source=messagesToSend.get(toSendIndex);
					}
				}
				if(source!=null)
				{
					lastSent=toSendIndex;
					int avail=source.getToSend().available();
					byte[] content=new byte[Math.min(avail, datagramMaxSize)];
					try {
						source.getToSend().read(content);
					} catch (IOException e) {/* bytearrayinputstream never fails*/ }
					boolean lastPiece=source.getToSend().available()<1;
					SocketMultiplexerDatagram datagram=new SocketMultiplexerDatagram(source.getId(), content, lastPiece);
					if(lastPiece)
					{
						synchronized (messagesToSend) {
							messagesToSend.remove(toSendIndex);
						}
					}
					try {
						streamer.writeToStream(datagram, os);
						os.flush();
					} catch (IOException e) {
						synchronized (messagesToSend) {
							disconnected=true;
						}
						messageListener.pipeBroken(e);
					}
					if(lastPiece)
					{
						source.sent();
					}
				}
			}
		}
	}
	public void addMessageToSend(byte[] messageContent, AbstractCoolRMIMessage message)
	{
		boolean b;
		synchronized (messagesToSend) {
			messagesToSend.add(new SocketMultiplexerSource(counter++, new ByteArrayInputStream(messageContent), message));
			messagesToSend.notifyAll();
			b=disconnected;
		}
		if(b)
		{
			message.sent();
		}
	}
	public void stop()
	{
		exit=true;
		List<SocketMultiplexerSource> toCancel=null;
		synchronized (messagesToSend) {
			messagesToSend.notifyAll();
			toCancel=new ArrayList<SocketMultiplexerSource>(messagesToSend);
		}
		if(toCancel!=null)
		{
			for(SocketMultiplexerSource s: toCancel)
			{
				s.sent();
			}
		}
	}
}
