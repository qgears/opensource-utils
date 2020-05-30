package hu.qgears.images.vnc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.qgears.commons.NamedThreadFactory;
import hu.qgears.commons.UtilChannel;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

/**
 * Simple VNC server implementation.
 */
public class VNCServer 
{
	private volatile boolean exit=false;
	private NativeImage currentImage;
	private ServerSocket ss;
	private PixelFormat pixelFormat;
	private SizeInt size;
	private String serverName="Simple embedded VNC Server";
	private RingBuffer events;
	private long updateCount=0;
	private List<VNCServerConnection> connections=Collections.synchronizedList(new ArrayList<VNCServerConnection>());
	private class VNCServerQuery
	{
		int x,y,w,h;
		boolean incremental;
		PixelFormat format;
	}
	private class VNCServerConnection extends Thread
	{
		NativeImage prevImage;
		NativeImage nextImage;
		ByteBuffer prevImageBuff;
		ByteBuffer nextImageBuff;
		String clientVersionString;
		SocketChannel s;
		PixelFormat current=pixelFormat;
		private int step;
		private long lastUpdateCount;
		private long thisUpdateCount;
		private volatile VNCServerQuery currentUpdateQuery;
		private ExecutorService exec=Executors.newSingleThreadExecutor(new NamedThreadFactory("VNC frame sender").setDaemon(true));
		private ByteBuffer sendByteBuffer=ByteBuffer.allocateDirect(Math.max(size.getWidth()*4+32, 1024)).order(ByteOrder.BIG_ENDIAN);
		private ByteBuffer frameByteBuffer=ByteBuffer.allocateDirect(Math.max(size.getWidth()*size.getHeight()*4+32*size.getHeight(), 1024)).order(ByteOrder.BIG_ENDIAN);
		public VNCServerConnection(SocketChannel s) {
			this.s=s;
			current.validate();
		}
		@Override
		public void run() {
			try {
				connections.add(this);
				prevImage=NativeImage.create(size, currentImage.getComponentOrder(), DefaultJavaNativeMemoryAllocator.getInstance());
				nextImage=NativeImage.create(size, currentImage.getComponentOrder(), DefaultJavaNativeMemoryAllocator.getInstance());
				prevImageBuff=prevImage.getBuffer().getJavaAccessor();
				nextImageBuff=nextImage.getBuffer().getJavaAccessor();
				step=prevImage.getStep();
				version();
				System.out.println("Version ok: "+clientVersionString);
				auth();
				System.out.println("Authorized");
				init();
				while(true)
				{
					UtilChannel.readNBytes(s, sendByteBuffer, 1);
					sendByteBuffer.flip();
					byte command=sendByteBuffer.get();
					switch (command) {
					case 0:
						messageSetPixelFormat();
						break;
					case 2:
						messageSetEncodings();
						break;
					case 3:
						messageFramebufferUpdateRequest();
						break;
					case 4:
						messageKeyEvent();
						break;
					case 5:
						messagePointerEvent();
						break;
					case 6:
						messageClientCutText();
						break;
					default:
						throw new RuntimeException("Unknown command: "+command);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally
			{
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				connections.remove(this);
				exec.shutdown();
			}
		}
		private void messageClientCutText() throws IOException {
			UtilChannel.readNBytes(s, sendByteBuffer, 3);
			VNCClient.readString(s, sendByteBuffer);	// TODO we ignore this
		}
		private void messagePointerEvent() throws IOException {
			sendByteBuffer.clear();
			sendByteBuffer.put((byte)5);
			sendByteBuffer.limit(6);
			UtilChannel.readNBytesAppend(s, sendByteBuffer);
			enqueueEvent(sendByteBuffer);
		}
		private void enqueueEvent(ByteBuffer sendByteBuffer) {
			int pad=VncEvent.storageSize-sendByteBuffer.position();
			if(pad<0)
			{
				throw new RuntimeException("Internal error");
			}
			sendByteBuffer.limit(VncEvent.storageSize);
			for(int i=0;i<pad;++i)
			{
				sendByteBuffer.put((byte)0);
			}
			sendByteBuffer.flip();
			try
			{
				// Write is synchronized for the case when there are multiple clients
				synchronized (events) {
					events.write(sendByteBuffer);
				}
			}catch(Exception e)
			{
				// Exception may happen in case the ringbuffer is full
				e.printStackTrace();
			}
		}
		private void messageKeyEvent() throws IOException {
			sendByteBuffer.clear();
			sendByteBuffer.put((byte)4);
			sendByteBuffer.limit(8);
			UtilChannel.readNBytesAppend(s, sendByteBuffer);
			enqueueEvent(sendByteBuffer);
		}
		private void messageFramebufferUpdateRequest() throws IOException {
			UtilChannel.readNBytes(s, sendByteBuffer, 9);
			sendByteBuffer.flip();
			VNCServerQuery q=new VNCServerQuery();
			q.incremental=sendByteBuffer.get()!=0;
			q.x=sendByteBuffer.getShort()&0xFFFF;
			q.y=sendByteBuffer.getShort()&0xFFFF;
			q.w=sendByteBuffer.getShort()&0xFFFF;
			q.h=sendByteBuffer.getShort()&0xFFFF;
			q.format=current;
			synchronized (VNCServerConnection.this) {
				currentUpdateQuery=q;
			}
			// System.out.println("Update req: "+q.x+" "+q.y+" "+q.w+" "+q.h+ "inc: "+q.incremental);
			sendUpdates();
		}
		private void sendUpdates() {
			exec.execute(new Runnable() {
				@Override
				public void run() {
					VNCServerQuery q;
					synchronized (VNCServerConnection.this) {
						q=currentUpdateQuery;
						currentUpdateQuery=null;
					}
					if(q==null)
					{
						return;
					}
					
					synchronized (currentImage) {
						copyRect(nextImage, currentImage, q.x, q.y, q.x, q.y,q.w, q.h);
						thisUpdateCount=updateCount;
					}
					try {
						if(!q.incremental)
						{
							sendRectangle(q.x,q.y,q.w,q.h, q.format);
							copyRect(prevImage, nextImage, q.x, q.y, q.x, q.y,q.w, q.h);
						}else
						{
							if(lastUpdateCount==thisUpdateCount)
							{
								synchronized (VNCServerConnection.this) {
									if(currentUpdateQuery==null)
									{
										currentUpdateQuery=q; // Skip this update and stash this runnable for later.
									}
								}
								// No change - send only when change happens
								return;
							}else
							{
								// System.out.println("Send increment "+thisUpdateCount);
								boolean hasChange=sendRectangleIncremental(q.x,q.y,q.w,q.h, q.format);
								if(!hasChange)
								{
									synchronized (VNCServerConnection.this) {
										if(currentUpdateQuery==null)
										{
											currentUpdateQuery=q; // Skip this update and stash this runnable for later.
										}
									}
									return;
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
		}
		private final static int minSamePixels=8;
		private final static int nPad=1;
		private boolean sendRectangleIncremental(int x, int y, int w, int h, PixelFormat format) throws IOException {
			frameByteBuffer.clear();
			frameByteBuffer.put((byte)0);
			for(int i=0;i<nPad;++i)
			{
				frameByteBuffer.put((byte)0);
			}
			frameByteBuffer.putShort((short)0);	// Will be overwritten at the end with the valid number
			
			ByteBuffer src=nextImage.getBuffer().getJavaAccessor().duplicate();
			int step=nextImage.getStep();
			format.validate();
			int N=0;
			int xmax=x+w;
			loop:
			for(int j=0;j<h;++j)
			{
				int xat=x;
				while(xat>=0)
				{
					xat=findFirstDiff(xat, y+j, xmax);
					if(xat>=0)
					{
						int l=findChangeLength(xat, y+j, xmax);
						frameByteBuffer.putShort((short)xat);
						frameByteBuffer.putShort((short)(y+j));
						frameByteBuffer.putShort((short)l);
						frameByteBuffer.putShort((short)1);
						frameByteBuffer.putInt(0);	// Encoding type
						format.encodeStrip(frameByteBuffer, step, src, xat, y+j, l);
						copyRect(prevImage, nextImage, xat, y+j, xat, y+j, l, 1);
						N++;
						if(N>=(int)Short.MAX_VALUE)
						{
							break loop;	// Would overflow - remaining changes are sent in a next iteration!
						}
						xat+=minSamePixels+l;
					}
				}
			}
			frameByteBuffer.putShort(nPad+1, (short)N);
			if(N==0)
			{
				return false;
			}
			if(N<(int)Short.MAX_VALUE)
			{
				lastUpdateCount=thisUpdateCount;
			}
			// System.out.println("N changes: "+frameByteBuffer.getShort(2)+" "+N);
			frameByteBuffer.flip();
			parseChanges(frameByteBuffer.duplicate());
			writeNBytes(frameByteBuffer);
			return true;
		}
		private void writeNBytes(ByteBuffer toSend) throws IOException
		{
			// System.out.println("Send: "+toSend.remaining());
			UtilChannel.writeNBytes(s, toSend);
		}
		private void parseChanges(ByteBuffer duplicate) {
			int type=duplicate.get();
			if(type!=0)
			{
				throw new RuntimeException();
			}
			for(int i=0;i<nPad;++i)
			{
				duplicate.get();
			}
			int N=duplicate.getShort();
			for(int i=0;i<N;++i)
			{
				int x=duplicate.getShort();
				if(x<0||x>=size.getWidth())
				{
					throw new RuntimeException();
				}
				int y=duplicate.getShort();
				if(y<0||y>=size.getHeight())
				{
					throw new RuntimeException("Para: "+x+" "+y+" "+i);
				}
				int w=duplicate.getShort();
				if(w<0||w+x>size.getWidth())
				{
					throw new RuntimeException();
				}
				int h=duplicate.getShort();
				if(h<1||h+y>size.getHeight())
				{
					throw new RuntimeException();
				}
				int typep=duplicate.getInt();
				if(typep!=0)
				{
					throw new RuntimeException();
				}
				int nbyte=w*h*current.bitsPerPixel/8;
				for(int j=0;j<nbyte;++j)
				{
					duplicate.get();
				}
			}
			if(duplicate.hasRemaining())
			{
				throw new RuntimeException();
			}
		}
		private int findChangeLength(int xat, int j, int xmax) {
			int l=1;
			int nSame=0;
			for(int x=xat+1;x<xmax && nSame<minSamePixels;++x)
			{
				int v0=prevImageBuff.getInt(x*4+j*step);
				int v1=nextImageBuff.getInt(x*4+j*step);
				if(v0==v1)
				{
					nSame++;
				}else
				{
					nSame=0;
				}
				l++;
			}
			l-=nSame;
			return l;
		}
		private int findFirstDiff(int xat, int j, int xmax) {
			for(int x=xat;x<xmax;++x)
			{
				int v0=prevImageBuff.getInt(x*4+j*step);
				int v1=nextImageBuff.getInt(x*4+j*step);
				if(v0!=v1)
				{
					return x;
				}
			}
			return -1;
		}
		private void sendRectangle(int x, int y, int w, int h, PixelFormat format) throws IOException {
			frameByteBuffer.clear();
			frameByteBuffer.put((byte)0);
			for(int i=0;i<1;++i)
			{
				frameByteBuffer.put((byte)0);
			}
			frameByteBuffer.putShort((short)1);
			
			frameByteBuffer.putShort((short)x);
			frameByteBuffer.putShort((short)y);
			frameByteBuffer.putShort((short)w);
			frameByteBuffer.putShort((short)h);
			frameByteBuffer.putInt(0);	// Encoding type
			ByteBuffer src=nextImage.getBuffer().getJavaAccessor().duplicate();
			int step=nextImage.getStep();
			format.validate();
			for(int j=0;j<h;++j)
			{
				format.encodeStrip(frameByteBuffer, step, src, x, y+j, w);
			}
			frameByteBuffer.flip();
			writeNBytes(frameByteBuffer);
			lastUpdateCount=thisUpdateCount;
		}
		private void messageSetEncodings() throws IOException {
			UtilChannel.readNBytes(s, sendByteBuffer, 3);
			sendByteBuffer.flip();
			sendByteBuffer.get();
			int n=sendByteBuffer.getShort()&0xFFFF;
			UtilChannel.readNBytes(s, sendByteBuffer, n*4);
			// Ignore encodings, only RAW is supported.
		}
		private void messageSetPixelFormat() throws IOException {
			UtilChannel.readNBytes(s, sendByteBuffer, 3+16);
			sendByteBuffer.flip();
			sendByteBuffer.get();
			sendByteBuffer.get();
			sendByteBuffer.get();
			PixelFormat requested=new PixelFormat();
			requested.parse(sendByteBuffer);
			System.out.println("Requested pixel format: "+requested);
			requested.validate();
			current=requested;
		}
		private void init() throws IOException {
			UtilChannel.readNBytes(s, sendByteBuffer, 1);
			sendByteBuffer.flip();
			byte command=sendByteBuffer.get();
			System.out.println("ClientInit received: "+command);
			sendByteBuffer.clear();
			sendByteBuffer.putShort((short)size.getWidth());
			sendByteBuffer.putShort((short)size.getHeight());
			pixelFormat.serialize(sendByteBuffer);
			VNCClient.writeStringAndLength(sendByteBuffer, serverName);
			sendByteBuffer.flip();
			writeNBytes(sendByteBuffer);
		}
		private void auth() throws IOException {
			sendByteBuffer.clear();
			sendByteBuffer.put((byte)1);
			sendByteBuffer.put((byte)1);
			sendByteBuffer.flip();
			writeNBytes(sendByteBuffer);
			// Only Auth mode is no auth

			UtilChannel.readNBytes(s, sendByteBuffer, 1);
			sendByteBuffer.flip();
			byte selectedMode=sendByteBuffer.get();
			if(selectedMode!=1)
			{
				throw new RuntimeException("Auth mode selected is not correct");
			}
			sendByteBuffer.clear();
			sendByteBuffer.putInt(0);
			sendByteBuffer.flip();
			writeNBytes(sendByteBuffer);
		}
		private void version() throws IOException {
			sendByteBuffer.clear();
			sendByteBuffer.put(VNCClient.versionString.getBytes(StandardCharsets.UTF_8));
			sendByteBuffer.flip();
			writeNBytes(sendByteBuffer);
			UtilChannel.readNBytes(s, sendByteBuffer, 12);
			sendByteBuffer.flip();
			clientVersionString=VNCClient.parseString(sendByteBuffer, 12);
		}
		public void frameUpdated() {
			sendUpdates();
		}
	}
	public VNCServer(SizeInt size)
	{
		this.size=size;
		pixelFormat=new PixelFormat();
		pixelFormat.init();
		events=new RingBuffer(ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder()));
		currentImage=NativeImage.create(size, ENativeImageComponentOrder.ARGB, DefaultJavaNativeMemoryAllocator.getInstance());
	}
	public void start(SocketAddress address, boolean daemon) throws IOException
	{
		ServerSocketChannel ss = ServerSocketChannel.open();
		ss.bind(address);
		Thread th=new Thread("VNC Server thread") {
			@Override
			public void run() {
				try {
					while(!exit)
					{
						SocketChannel s=ss.accept();
						VNCServerConnection conn=new VNCServerConnection(s);
						conn.setDaemon(daemon);
						conn.start();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		th.setDaemon(daemon);
		th.start();
	}
	public void dispose()
	{
		exit=true;
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateFrame(NativeImage frameBuffer) {
		synchronized (currentImage) {
			if(currentImage.getComponentOrder()!=frameBuffer.getComponentOrder())
			{
				throw new RuntimeException("Invalid Component order: "+frameBuffer.getComponentOrder());
			}
			currentImage.copyFromSource(frameBuffer, 0, 0);
			updateCount++;
		}
		synchronized (connections) {
			for(VNCServerConnection c:connections)
			{
				c.frameUpdated();
			}
		}
	}
	private VncEvent event=new VncEvent();
	public VncEvent pollEvent() {
		while(events.readAvailable()>=VncEvent.storageSize)
		{
			event.readFrom(events);
			return event;
		}
		return null;
	}
	
	private void copyRect(NativeImage dst, NativeImage src, int x, int y, int tgX, int tgY, int w, int h) {
		int nc = src.getnChannels();
		ByteBuffer srcb = src.getBuffer().getJavaAccessor().duplicate();
		ByteBuffer bb = dst.getBuffer().getJavaAccessor().duplicate();
		int stepSrc = src.getStep();
		int stepTrg = dst.getStep();
		for (int j = 0; j < h; ++j) {
			int ptrsrc = (j + y) * stepSrc + x * nc;
			srcb.limit(ptrsrc + w * nc);
			srcb.position(ptrsrc);
			bb.position((j + tgY) * stepTrg + tgX * nc);
			bb.put(srcb);
		}
	}
}
