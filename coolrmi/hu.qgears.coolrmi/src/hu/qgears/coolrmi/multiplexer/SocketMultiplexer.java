package hu.qgears.coolrmi.multiplexer;

import hu.qgears.coolrmi.CoolRMIObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;



/**
 * Multiplexes several messages on a single TCP
 * connection to both direction.
 * 
 * Both write and read has an own thread.
 * 
 * @author rizsi
 *
 */
public class SocketMultiplexer {
	private boolean guaranteeOrdering;
	ISocketMultiplexerListener messageListener;
	InputStream is;
	public SocketMultiplexer(InputStream is, OutputStream os,
			ISocketMultiplexerListener messageListener,
			boolean guaranteeOrdering) throws IOException {
		super();
		this.is = is;
		this.os = os;
		this.messageListener=messageListener;
		this.oos=new ObjectOutputStream(os);
	}
	public void start()
	{
		new ReadThread().start();
		new WriteThread().start();
	}
	OutputStream os;
	ObjectOutputStream oos;
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
				CoolRMIObjectInputStream ois=new CoolRMIObjectInputStream(SocketMultiplexer.class.getClassLoader(), is);
				while(!exit)
				{
					SocketMultiplexerDatagram datagram=(SocketMultiplexerDatagram) ois.readObject();
					long id=datagram.getDatagramId();
					ByteArrayOutputStream bos=getMessage(id);
					bos.write(datagram.content);
					if(datagram.isLastPiece())
					{
						removeMessage(id);
						messageListener.messageReceived(bos.toByteArray());
					}
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
					SocketMultiplexerDatagram datagram=new SocketMultiplexerDatagram();
					int avail=source.getToSend().available();
					byte[] content=new byte[Math.min(avail, datagramMaxSize)];
					try {
						source.getToSend().read(content);
					} catch (IOException e) {/* bytearrayinputstream never fails*/ }
					datagram.setContent(content);
					datagram.setDatagramId(source.getId());
					boolean lastPiece=source.getToSend().available()<1;
					datagram.setLastPiece(lastPiece);
					if(lastPiece)
					{
						synchronized (messagesToSend) {
							messagesToSend.remove(toSendIndex);
						}
					}
					try {
						oos.writeObject(datagram);
						oos.flush();
					} catch (IOException e) {
						messageListener.pipeBroken(e);
					}
				}
			}
		}
	}
	boolean exit=false;
	long counter=0;
	LinkedList<SocketMultiplexerSource> messagesToSend=new LinkedList<SocketMultiplexerSource>();
	public void addMessageToSend(byte[] messageContent)
	{
		synchronized (messagesToSend) {
			messagesToSend.add(new SocketMultiplexerSource(counter++, new ByteArrayInputStream(messageContent)));
			messagesToSend.notifyAll();
		}
	}
	public void stop()
	{
		exit=true;
		synchronized (messagesToSend) {
			messagesToSend.notifyAll();
		}
	}
}
