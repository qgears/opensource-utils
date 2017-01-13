package hu.qgears.coolrmi.remoter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.qgears.commons.NamedThreadFactory;
import hu.qgears.coolrmi.CoolRMIClose;
import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.CoolRMIShareableObject;
import hu.qgears.coolrmi.CoolRMITimeoutException;
import hu.qgears.coolrmi.ICoolRMIProxy;
import hu.qgears.coolrmi.ICoolRMIServerSideProxy;
import hu.qgears.coolrmi.messages.AbstractCoolRMICall;
import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;
import hu.qgears.coolrmi.messages.AbstractCoolRMIReply;
import hu.qgears.coolrmi.messages.CoolRMICreateProxy;
import hu.qgears.coolrmi.messages.CoolRMICreateProxyReply;
import hu.qgears.coolrmi.messages.CoolRMIDisposeProxy;
import hu.qgears.coolrmi.messages.CoolRMIFutureReply;
import hu.qgears.coolrmi.messages.CoolRMIProxyPlaceHolder;
import hu.qgears.coolrmi.messages.CoolRMIRequestServiceQuery;
import hu.qgears.coolrmi.messages.CoolRMIRequestServiceReply;
import hu.qgears.coolrmi.multiplexer.ISocketMultiplexerListener;
import hu.qgears.coolrmi.multiplexer.SocketMultiplexer;
import hu.qgears.coolrmi.streams.IConnection;



public class CoolRMIRemoter {
	private class SocketMultiplexerListener implements ISocketMultiplexerListener
	{

		@Override
		public void messageReceived(byte[] msg) {
			CoolRMIRemoter.this.messageReceived(msg);
		}

		@Override
		public void pipeBroken(Exception e) {
			CoolRMIRemoter.this.pipeBroken(e);
		}
		
	}
	private SocketMultiplexer multiplexer;
	private long timeoutMillis=30000;
	private IConnection sock;
	private ClassLoader classLoader;
	private boolean connected = false;
	private boolean closed = false;
	private Executor serverSideExecutor = null;
	private boolean guaranteeOrdering;
	private Map<Long, CoolRMIFutureReply> replies = new HashMap<Long, CoolRMIFutureReply>();
	/**
	 * The client side proxy objects.
	 */
	private Map<Long, CoolRMIProxy> proxies = Collections
			.synchronizedMap(new HashMap<Long, CoolRMIProxy>());
	/**
	 * The server side service objects that have proxies on the other side.
	 */
	private Map<Long, CoolRMIServerSideObject> services = Collections
			.synchronizedMap(new HashMap<Long, CoolRMIServerSideObject>());
	private long callCounter = 0;
	private long proxyCounter = 0;
	
	public CoolRMIRemoter(ClassLoader classLoader, boolean guaranteeOrdering) {
		this.classLoader = classLoader;
		this.guaranteeOrdering=guaranteeOrdering;
		if(guaranteeOrdering)
		{
			serverSideExecutor=Executors.newSingleThreadExecutor(new NamedThreadFactory("Cool RMI executor"));
		}else
		{
			serverSideExecutor=new Executor()
			{
				@Override
				public void execute(Runnable command) {
					Thread th=new Thread(command, "Cool RMI executor");
					th.start();
				}
			};
		}
	}

	public void setTimeoutMillis(long timeout) {
		this.timeoutMillis = timeout;
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
	protected void connect(IConnection sock) throws IOException {
		this.sock = sock;
		multiplexer = new SocketMultiplexer(
				sock.getInputStream(), sock
				.getOutputStream(), new SocketMultiplexerListener(), guaranteeOrdering);
		connected = true;
		multiplexer.start();
	}

	/**
	 * Remove proxy object from this remoting home.
	 * 
	 * @param coolRMIProxy
	 * @throws IOException
	 */
	protected void remove(CoolRMIProxy coolRMIProxy) {
		synchronized (proxies) {
			proxies.remove(coolRMIProxy.getId());
		}
		CoolRMIDisposeProxy message = new CoolRMIDisposeProxy(getNextCallId(),
				coolRMIProxy.getId());
		try {
			send(message);
		} catch (IOException e) {/* Must never happen - but who knows? */
			e.printStackTrace();
		}
	}

	public void send(AbstractCoolRMIMessage message) throws IOException {
		byte[] bs = UtilSerializator.serialize(servicesReg, message);
		multiplexer.addMessageToSend(bs, message.getName());
	}
	/**
	 * Send a message call to the other side. Send is asynchronous. Reply can be
	 * waited by getReply() method.
	 * 
	 * @param call
	 * @throws IOException
	 */
	public void sendCall(AbstractCoolRMICall call) throws IOException {
		send(call);
	}

	public synchronized long getNextCallId() {
		return callCounter++;
	}

	private synchronized long getNextProxyId() {
		return proxyCounter++;
	}

	public void messageReceived(byte[] msg) {
		try {
			Object message = UtilSerializator.deserialize(msg, classLoader);
			if (message instanceof AbstractCoolRMICall) {
				AbstractCoolRMICall call = (AbstractCoolRMICall) message;
				doCall(call);
			} else if (message instanceof CoolRMIClose) {
				close();
			} else if (message instanceof CoolRMIRequestServiceQuery) {
				handleRequestServiceQuery((CoolRMIRequestServiceQuery) message);
			} else if (message instanceof CoolRMIRequestServiceQuery) {
				handleRequestServiceQuery((CoolRMIRequestServiceQuery) message);
			} else if (message instanceof AbstractCoolRMIReply) {
				handleReply((AbstractCoolRMIReply) message);
			} else if (message instanceof CoolRMIDisposeProxy) {
				handleDisposeProxy((CoolRMIDisposeProxy) message);
			} else if (message instanceof CoolRMICreateProxy)
			{
				handleCreateProxy((CoolRMICreateProxy)message);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleCreateProxy(CoolRMICreateProxy message) throws ClassNotFoundException, IOException {
		Class<?> ifaceClass=Class.forName(message.getIfaceName(), false, classLoader);
		CoolRMIProxy proxy=new CoolRMIProxy(this,
				message.getProxyId(),
				ifaceClass);
		proxies.put(proxy.getId(), proxy);
		send(new CoolRMICreateProxyReply(message.getQueryId()));
	}

	private void handleDisposeProxy(CoolRMIDisposeProxy message) {
		CoolRMIServerSideObject service = services.remove(message.getProxyId());
		if(service!=null)
		{
			service.dispose(serverSideExecutor);
		}
	}

	CoolRMIServiceRegistry servicesReg = new CoolRMIServiceRegistry();

	public CoolRMIServiceRegistry getServiceRegistry() {
		return servicesReg;
	}

	public void setServiceRegistry(CoolRMIServiceRegistry servicesReg) {
		this.servicesReg = servicesReg;
	}

	private void handleRequestServiceQuery(CoolRMIRequestServiceQuery message)
			throws IOException {
		CoolRMIService service = servicesReg.getService(message
				.getServiceName());
		if (service == null) {
			send(new CoolRMIRequestServiceReply(message.getQueryId(), -1, null));
		} else {
			CoolRMIServerSideObject sso = createProxyObject(service);
			send(new CoolRMIRequestServiceReply(message.getQueryId(), sso
					.getProxyId(), sso.getIface().getName()));
		}
	}

	private void handleReply(AbstractCoolRMIReply reply) {
		CoolRMIFutureReply future;
		synchronized (replies) {
			future=replies.remove(reply.getQueryId());
		}
		if(future!=null)
		{
			future.received(reply);
		}
	}
	private void doCall(final AbstractCoolRMICall abstarctCall) throws IOException {
		abstarctCall.executeServerSide(this, serverSideExecutor);
	}

	protected Object[] resolveProxyInParamersServerSide(Object[] args) throws IOException {
		if(args!=null)
		{
			for(int i=0;i<args.length;++i)
			{
				args[i]=resolveProxyInParamerServerSide(args[i]);
			}
		}
		return args;
	}
	public Object[] resolveProxyInParamersClientSide(Object[] args) {
		if(args!=null)
		{
			for(int i=0;i<args.length;++i)
			{
				args[i]=resolveProxyInParamerClientSide(args[i]);
			}
		}
		return args;
	}
	public Object resolveProxyInParamerServerSide(Object arg) throws IOException {
		CoolRMIServiceRegistry reg=getServiceRegistry();
		if(arg!=null)
		{
			Class<?> iftype=reg.getProxyType(arg.getClass());
			if(iftype!=null)
			{
				ICoolRMIServerSideProxy ssop=createServerSideProxyObject(new CoolRMIShareableObject(iftype, arg));
				CoolRMIServerSideObject sso=ssop.getCoolRMIServerSideProxyObject();
				CoolRMIProxyPlaceHolder ph=new CoolRMIProxyPlaceHolder(sso.getProxyId());
				return ph;
			}
		}
		if(arg instanceof ICoolRMIServerSideProxy)
		{
			CoolRMIServerSideObject sso=((ICoolRMIServerSideProxy) arg).getCoolRMIServerSideProxyObject();
			CoolRMIProxyPlaceHolder ph=new CoolRMIProxyPlaceHolder(sso.getProxyId());
			return ph;
		}
		return arg;
	}
	public Object resolveProxyInParamerClientSide(Object arg) {
		if(arg instanceof CoolRMIProxyPlaceHolder)
		{
			CoolRMIProxyPlaceHolder ph=(CoolRMIProxyPlaceHolder) arg;
			CoolRMIProxy proxy=proxies.get(ph.getProxyId());
			if(proxy!=null)
			{
				return proxy.getProxyObject();
			}else
			{
				return null;
			}
		}else
		{
			return arg;
		}
	}

	public void pipeBroken(Exception e) {
		try {
			close();
		} catch (IOException e1) {
			// There is nothing to do when the connection can not be properly closed.
			e1.printStackTrace();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean isClosed() {
		return closed;
	}

	public void close() throws IOException {
		connected = false;
		closed = true;
		multiplexer.stop();
		sock.close();
		if(serverSideExecutor instanceof ExecutorService)
		{
			((ExecutorService) serverSideExecutor).shutdown();
		}
		synchronized (replies) {
			// Queries waiting for replies must be notified to close at once.
			replies.notifyAll();
		}
	}
	/**
	 * Create future reply object.
	 * Must be called before sending the query!
	 * @return the reply object
	 * @throws CoolRMITimeoutException in case of timeout (see setTimeout)
	 * @throws CoolRMIException in case of connection closed
	 */
	protected CoolRMIFutureReply getAbstractReply(long callId) {
		CoolRMIFutureReply ret=new CoolRMIFutureReply(this, callId);
		synchronized (replies) {
			replies.put((Long)callId, ret);
		}
		return ret;
	}

	/**
	 * Create a client proxy of the specified service. The method will not
	 * connect to the server. <br/>
	 * The generated proxy object will connect and disconnect to the server on
	 * each query (method call). Invalid service name or incompatible interface
	 * problems will only be reported when using the interface.
	 * 
	 * User exceptions are passed from the server if occur. Communication
	 * related problems are thrown as CoolRMIException. It is a RuntimeException
	 * so users may not handle them.
	 * 
	 * @param classLoader
	 *            The classloader used for message serialization. Must see
	 *            CoolRMI and the communication interface.
	 * @param iface
	 *            The communication interface. Must be compatible (to serial
	 *            version) with the one deployed on the server.
	 * @param serviceName
	 *            The service name.
	 * @return The client proxy to the given service. Will implement the passed
	 *         interface
	 * @throws IOException
	 */
	public ICoolRMIProxy getService(Class<?> iface, String serviceName)
			throws IOException {
		CoolRMIRequestServiceQuery query = new CoolRMIRequestServiceQuery(
				getNextCallId(), serviceName);
		CoolRMIFutureReply replyFuture=getAbstractReply(query.getQueryId());
		send(query);
		CoolRMIRequestServiceReply reply = (CoolRMIRequestServiceReply) replyFuture.waitReply();
		CoolRMIProxy proxy = new CoolRMIProxy(this, reply.getProxyId(), iface);
		proxies.put(proxy.getId(), proxy);

		return proxy.getProxyObject();
	}

	private CoolRMIServerSideObject createProxyObject(
			CoolRMIShareableObject service) {
		Object impl = service.getService();
		Class<?> iface = service.getInterface();
		CoolRMIServerSideObject ret = new CoolRMIServerSideObject(
				getNextProxyId(), iface, impl);
		services.put(ret.getProxyId(), ret);
		return ret;
	}
	private ICoolRMIServerSideProxy createServerSideProxyObject(CoolRMIShareableObject service) throws IOException
	{
		CoolRMIServerSideObject sso = createProxyObject(service);
		CoolRMIServerSideProxy ret=new CoolRMIServerSideProxy(this, sso);
		CoolRMICreateProxy req=new CoolRMICreateProxy(getNextCallId(), sso.getProxyId(), sso.getIface().getName());
		CoolRMIFutureReply replyFut=getAbstractReply(req.getQueryId());
		send(req);
		replyFut.waitReply();
		return ret.getProxyObject();
	}

	public CoolRMIServerSideObject getProxyById(long proxyId) {
		return services.get(proxyId);
	}

	public void removeAwaitingReply(CoolRMIFutureReply coolRMIFutureReply) {
		synchronized (replies) {
			replies.remove(coolRMIFutureReply.getCallId());
		}
	}
}
