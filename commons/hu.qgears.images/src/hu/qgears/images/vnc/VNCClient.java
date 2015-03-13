package hu.qgears.images.vnc;

import hu.qgears.commons.UtilChannel;
import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.LazyNativeImage;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In memory VNC client implementation. Can be attached to
 * GUI technology easily.
 * Simplifications:
 *  * Only handles a single pixel format
 *  * Only handles RAW rectangle data
 *  * Does not handle colour map entries
 *  * Does not handle keyboard input
 *  * If disconnected or any other error occurs tries to re-connect.
 * @author rizsi
 *
 */
public class VNCClient {
	private ENativeImageComponentOrder componentOrder;
	private long retryTimeoutMillis=1000;
	private LazyNativeImage image=new LazyNativeImage();
	private LazyNativeImage nextImage=new LazyNativeImage();
	private static final String versionString="RFB 003.008\n";
	private String host="localhost";
	private int port=5902;
	private volatile int changeCounter=0;

	private ByteBuffer sendByteBuffer=ByteBuffer.allocateDirect(1000).order(ByteOrder.BIG_ENDIAN);
	private SocketChannel channel;
	private PixelFormat pixelFormat;
	private short width;
	private short height;
	volatile private boolean exit=false;
	private volatile boolean connected=false;
	private ReentrantLock lock=new ReentrantLock();
	/**
	 * Start the vnc client by connecting to host and port.
	 * @param host hostname
	 * @param port TCP port
	 */
	public void start(String host, int port)
	{
		this.host=host;
		this.port=port;
		new Thread("VNC client"){
			public void run() {
				try {
					VNCClient.this.run();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			};
		}.start();
	}
	/**
	 * Stop the VNC client and free all resources.
	 */
	public void dispose()
	{
		synchronized (this) {
			exit=true;
			if(channel!=null)
			{
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Get a snapshot of the image on the VNC client.
	 * 
	 * The returned snapshot is synchronized with the VLC client
	 * but only with methods that don't read data from the TCP stream
	 * so they can not be blocked by network latency.
	 * 
	 * The returned snapshot is always a fully updated image state (that is present at the
	 * end of the frameBufferUpdate VNC message) no half rendered is returned.
	 * 
	 * @return The returned snapshot must be freed by the client after copying it to the
	 * output (eg. screen). This call locks further updates on the buffer until it is freed.
	 */
	public VncSnapShot getSnapShot()
	{
		lock.lock();
		return new VncSnapShot(this, image.getCurrentImage(), changeCounter);
	}
	private void run() throws IOException
	{
		while(!exit)
		{
			try
			{
				componentOrder=ENativeImageComponentOrder.RGBA;
				synchronized (this) {
					if(!exit)
					{
						channel = SocketChannel.open();
					}
				}
				try
				{
					channel.connect(new InetSocketAddress(host, port));
					
					version();
					auth();
					init();
					setPixelFormat();
					frameBufferUpdateRequest();
					processServerMessages();
				}finally
				{
					connected=false;
					channel.close();
					changeCounter++;
				}
			}catch(Exception e)
			{
				// TODO
			//	e.printStackTrace();
			}
			channel=null;
			if(!exit)
			{
				try {
					Thread.sleep(retryTimeoutMillis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(channel!=null)
		{
			try {
				channel.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			channel=null;
		}
		image.dispose();
		nextImage.dispose();
	}
	private void processServerMessages() throws IOException {
		image.getImage(new SizeInt(width, height), componentOrder, DefaultJavaNativeMemoryAllocator.getInstance());
		nextImage.getImage(new SizeInt(width, height), componentOrder, DefaultJavaNativeMemoryAllocator.getInstance());
		ByteBuffer bb=ByteBuffer.allocateDirect(4*width*height).order(ByteOrder.BIG_ENDIAN);
		while(!exit)
		{
			bb.clear();
			UtilChannel.readNBytes(channel, bb, 1);
			bb.flip();
			byte command=bb.get();
			switch (command) {
			case 0:
				processFrameBufferUpdate(bb);
				connected=true;
				frameBufferUpdateRequest();
				break;
			case 1:
				processSetColourMapEntries(bb);
				break;
			case 2:
				processBell(bb);
				break;
			case 3:
				processServerCutText(bb);
				break;
			default:
				break;
			}
		}
	}
	private void processServerCutText(ByteBuffer bb) throws IOException {
		bb.clear();
		UtilChannel.readNBytes(channel, bb, 3); // padding
		bb.flip();
		// Received string is not used by us
		readString(bb);
	}
	private void processBell(ByteBuffer bb) {
		// Nothing to do, it is a single byte message :-)
	}
	private void processSetColourMapEntries(ByteBuffer bb2) {
		// This mode is not implemented as our server does not send such messages
		throw new RuntimeException("SetColourMapEntries");
	}
	private void processFrameBufferUpdate(ByteBuffer bb) throws IOException {
		bb.clear();
		UtilChannel.readNBytes(channel, bb, 3); // padding
		bb.flip();
		bb.get(); // padding
		short nRect=bb.getShort();
		for(int i=0;i<nRect;++i)
		{
			receiveRectangle(bb);
		}
		lock.lock();
		try
		{
			image.getCurrentImage().copyFromSource(nextImage.getCurrentImage(), 0, 0);
			changeCounter++;
		}finally
		{
			lock.unlock();
		}
	}
	/**
	 * Get the number of changes since start.
	 * The user of the VNCClient component must redraw its output
	 * when this counter is updated.
	 * @return if the number of changes is more than 0 then this component is active
	 */
	public int getChangeCounter()
	{
		return changeCounter;
	}
	/**
	 * Get the size of the VNC server screen.
	 * @return
	 */
	public SizeInt getSize()
	{
		return new SizeInt(width, height);
	}
	private void receiveRectangle(ByteBuffer bb) throws IOException {
		bb.clear();
		UtilChannel.readNBytes(channel, bb, 12); // padding
		bb.flip();
		short x=bb.getShort();
		short y=bb.getShort();
		short w=bb.getShort();
		short h=bb.getShort();
		int encoding=bb.getInt();
		check("Rectangle encoding", encoding, 0);
		bb.clear();
		UtilChannel.readNBytes(channel, bb, 4*w*h);
		bb.flip();
		mergeWithImage(x, y, w, h, bb);
	}
	private void mergeWithImage(short x, short y, short w, short h,
			ByteBuffer bb) {
		if(x==0 && y==0 && w==width && h==height)
		{
			// Bulk
			ByteBuffer accessor=nextImage.getCurrentImage().getBuffer().getJavaAccessor();
			accessor.clear();
			accessor.put(bb);
		}else
		{
			// Line by line
			INativeMemory buffer=new DefaultJavaNativeMemory(bb);
			NativeImage src=new NativeImage(buffer, new SizeInt(w, h),
					componentOrder, 4);
			nextImage.getCurrentImage().copyFromSource(src, 0, 0, x, y);
		}
	}
	private void setPixelFormat() throws IOException {
		pixelFormat.init();
		sendByteBuffer.clear();
		sendByteBuffer.put((byte)0);
		sendByteBuffer.put((byte)0);
		sendByteBuffer.put((byte)0);
		sendByteBuffer.put((byte)0);
		pixelFormat.serialize(sendByteBuffer);
		sendByteBuffer.flip();
		UtilChannel.writeNBytes(channel, sendByteBuffer);
	}
	private void frameBufferUpdateRequest() throws IOException
	{
		synchronized (sendByteBuffer) {
			sendByteBuffer.clear();
			sendByteBuffer.put((byte)3);
			sendByteBuffer.put((byte)1); // incremental
			sendByteBuffer.putShort((short)0); // x
			sendByteBuffer.putShort((short)0); // y
			sendByteBuffer.putShort(width); // y
			sendByteBuffer.putShort(height); // y
			sendByteBuffer.flip();
			UtilChannel.writeNBytes(channel, sendByteBuffer);
		}
	}
	/**
	 * Send mouse event to the server.
	 * @param x coordinate as VNC specifies
	 * @param y coordinate as VNC specifies
	 * @param buttonMask Mouse button mask as VNC specifies
	 * @throws Exception in case the event could not be sent to the server for some reason
	 */
	public void mouseEvent(short x, short y, byte buttonMask) throws Exception
	{
		if(connected)
		{
			synchronized (sendByteBuffer) {
				sendByteBuffer.clear();
				sendByteBuffer.put((byte)5);
				sendByteBuffer.put(buttonMask); // incremental
				sendByteBuffer.putShort(x);
				sendByteBuffer.putShort(y);
				sendByteBuffer.flip();
				UtilChannel.writeNBytes(channel, sendByteBuffer);
			}
		}
	}
	private void init() throws IOException {
		sendByteBuffer.clear();
		sendByteBuffer.put((byte)1);
		sendByteBuffer.flip();
		UtilChannel.writeNBytes(channel, sendByteBuffer);
		sendByteBuffer.clear();
		UtilChannel.readNBytes(channel, sendByteBuffer, 20);
		sendByteBuffer.flip();
		width=sendByteBuffer.getShort();
		height=sendByteBuffer.getShort();
		pixelFormat=new PixelFormat();
		// We don't actually use the serves's pixel format but require the server to use ours.
		// Se we don't need to parse it but we do that for fun.
		pixelFormat.parse(sendByteBuffer);
		
		// We don't need the name of the server
		readString(sendByteBuffer);
	}
	private String readString(ByteBuffer bb) throws IOException
	{
		bb.clear();
		UtilChannel.readNBytes(channel, bb, 4);
		bb.flip();
		int l=bb.getInt();
		bb.clear();
		UtilChannel.readNBytes(channel, bb, l);
		bb.flip();
		return parseString(bb, l);
	}
	private void auth() throws IOException {
		sendByteBuffer.clear();
		UtilChannel.readNBytes(channel, sendByteBuffer, 1);
		sendByteBuffer.flip();
		byte n=sendByteBuffer.get();
		boolean noauthsupported=false;
		for(int i=0;i<n;++i)
		{
			sendByteBuffer.clear();
			UtilChannel.readNBytes(channel, sendByteBuffer, 1);
			sendByteBuffer.flip();
			byte mode=sendByteBuffer.get();
			if(mode==1)
			{
				noauthsupported=true;
			}
		}
		if(!noauthsupported)
		{
			throw new RuntimeException("No authorization is not supported by the server.");
		}
		
		sendByteBuffer.clear();
		sendByteBuffer.put((byte)1);
		sendByteBuffer.flip();
		UtilChannel.writeNBytes(channel, sendByteBuffer);
		sendByteBuffer.clear();
		UtilChannel.readNBytes(channel, sendByteBuffer, 4);
		sendByteBuffer.flip();
		int result=sendByteBuffer.getInt();
		if(result!=0)
		{
			String s=readString(sendByteBuffer);
			throw new RuntimeException("Authentication error received from server: "+s);
		}
	}
	private void version() throws IOException {
		UtilChannel.readNBytes(channel, sendByteBuffer, 12);
		sendByteBuffer.flip();
		parseString(sendByteBuffer, 12);
		// TODO check server version
		sendByteBuffer.clear();
		writeString(sendByteBuffer, versionString, versionString.length());
		sendByteBuffer.flip();
		UtilChannel.writeNBytes(channel, sendByteBuffer);
	}
	static final private String parseString(ByteBuffer params, int length)
	{
		byte[] bs=new byte[length];
		params.get(bs);
		int i=0;
		for(;i<length;++i)
		{
			if(bs[i]==0)
			{
				break;
			}
		}
		return new String(bs, 0, i, Charset.forName("UTF-8"));
	}
	final private static void writeString(ByteBuffer bb, String string, int length) {
		byte[] bs=new byte[length];
		byte[] src=string.getBytes(Charset.forName("UTF-8"));
		int i=0;
		for(;i<length;++i)
		{
			if(i>=src.length)
			{
				bs[i]=0;
			}else
			{
				bs[i]=src[i];
			}
		}
		bb.put(bs);
	}
	private void check(String string, int depth2, int i) {
		if(depth2!=i)
		{
			throw new RuntimeException(""+string+" must be "+i+" (and is: "+depth2+")");
		}
	}
	/**
	 * Internal API.
	 */
	protected void released() {
		lock.unlock();
	}
	public void keyboardEvent(int code, long timeStamp) throws IOException {
		if(connected)
		{
			synchronized (sendByteBuffer) {
				sendByteBuffer.clear();
				sendByteBuffer.put((byte)4);
				sendByteBuffer.put((byte)1);
				sendByteBuffer.putShort((short)0);
				sendByteBuffer.putInt(code);
				sendByteBuffer.flip();
				UtilChannel.writeNBytes(channel, sendByteBuffer);
				sendByteBuffer.clear();
				sendByteBuffer.put((byte)4);
				sendByteBuffer.put((byte)0);
				sendByteBuffer.putShort((short)0);
				sendByteBuffer.putInt(code);
				sendByteBuffer.flip();
				UtilChannel.writeNBytes(channel, sendByteBuffer);
			}
		}
	}
	public boolean isConnected() {
		return connected;
	}
}
