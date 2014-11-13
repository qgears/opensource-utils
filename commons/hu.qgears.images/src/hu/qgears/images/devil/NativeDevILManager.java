package hu.qgears.images.devil;

import hu.qgears.commons.NamedThreadFactory;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.UtilSignalFuture;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.nativeloader.UtilNativeLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NativeDevILManager {
	private static NativeDevILManager instance;
	private ExecutorService executor;
	private NativeDevIL singleton;
	synchronized public static NativeDevILManager getInstance() throws Exception {
		if(instance==null)
		{
			instance=new NativeDevILManager();
		}
		return instance;
	}
	private NativeDevILManager() throws Exception {
		UtilNativeLoader.loadNatives(new DevILAccessor());
		new NativeDevIL().initDevIL();
		NamedThreadFactory fact=new NamedThreadFactory("devIL image loader");
		fact.setPriority(Thread.MIN_PRIORITY);
		fact.setDaemon(true);
		executor=Executors.newSingleThreadExecutor(fact);
	}
	/**
	 * Create a native devIL connector.
	 * This method is public for test purposes only. Use loadImage methods on this class.
	 * @return
	 * @throws FileNotFoundException
	 */
	public NativeDevIL createDevIL() throws FileNotFoundException
	{
		NativeDevIL ret=new NativeDevIL();
		ret.init();
		return ret;
	}
	public SignalFuture<NativeImage> loadImage(final INativeMemoryAllocator allocator, final ByteBuffer content, final String ext)
	{
		return UtilSignalFuture.submit(executor, new Callable<NativeImage>() {
			@Override
			public NativeImage call() throws Exception {
				return loadImagePrivate(allocator, content, ext);
			}
		});
	}
	public SignalFuture<NativeImage> loadImage(final INativeMemoryAllocator allocator, final byte[] content, final String ext)
	{
		return UtilSignalFuture.submit(executor, new Callable<NativeImage>() {
			@Override
			public NativeImage call() throws Exception {
				return loadImagePrivate(allocator, content, ext);
			}
		});
	}
	public SignalFuture<NativeImage> loadImage(final INativeMemoryAllocator allocator, final File f)
	{
		return UtilSignalFuture.submit(executor, new Callable<NativeImage>() {
			@Override
			public NativeImage call() throws Exception {
				return loadImagePrivate(allocator, f);
			}
		});
	}
	private NativeDevIL getSingleton() throws FileNotFoundException
	{
		if(singleton==null)
		{
			singleton=createDevIL();
		}
		return singleton;
	}
	protected NativeImage loadImagePrivate(INativeMemoryAllocator allocator, ByteBuffer content, String ext) throws FileNotFoundException {
		getSingleton();
		singleton.load(content, ext);
		return singleton.copyBuffer(allocator);
	}
	protected NativeImage loadImagePrivate(INativeMemoryAllocator allocator, File f) throws IOException {
		// TODO load file into direct buffer directly!
		byte[] contnet=UtilFile.loadFile(f);
		return loadImagePrivate(allocator, contnet, f.getName());
	}
	protected NativeImage loadImagePrivate(INativeMemoryAllocator allocator, byte[] content, String ext) throws FileNotFoundException {
		getSingleton();
		singleton.load(content, ext);
		return singleton.copyBuffer(allocator);
	}
	public SignalFuture<NativeImage> saveImage(NativeImage out, final File file) {
		boolean needConvert=false;
		if(out.getWidth()%out.getAlignment()!=0)
		{
			needConvert=true;
		}
		if(!ENativeImageComponentOrder.RGBA.equals(out.getComponentOrder()))
		{
			needConvert=true;
		}
		if(needConvert)
		{
			NativeImage converted=NativeImage.create(out.getSize(), ENativeImageComponentOrder.RGBA,
					1,
					DefaultJavaNativeMemoryAllocator.getInstance());
			ByteBuffer b=converted.getBuffer().getJavaAccessor();
			b.clear();
			for(int i=0;i<b.capacity();++i)
			{
				b.put((byte)0);
			}
			converted.copyFromSource(out, 0, 0);
			out=converted;
		}
		final NativeImage outImage=out;
		return UtilSignalFuture.submit(executor, new Callable<NativeImage>() {
			@Override
			public NativeImage call() throws Exception {
				return saveImagePrivate(outImage, file);
			}
		});
	}
	protected NativeImage saveImagePrivate(NativeImage outImage, File file) throws FileNotFoundException {
		getSingleton();
		singleton.save(outImage, ENativeImageComponentOrder.RGBA, file);
		return outImage;
	}
}
