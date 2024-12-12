package hu.qgears.images;

import hu.qgears.commons.IDisposeable;
import hu.qgears.commons.mem.INativeMemoryAllocator;

/**
 * Lazy init framework for native image instantiation with changing parameters.
 * 
 * This class is easy to use in cases when:
 *  * we use an image buffer in a loop (repeatedly)
 *  * we want to cache the image buffer - ie not to reallocate it all the times
 *  * in case the required size or component order changes we have to re-allocate the image
 * 
 * @author rizsi
 *
 */
public class LazyNativeImage implements IDisposeable {
	private NativeImage im;
	/**
	 * Get the cached image instance or (re-)create it in case of parameters mismatch.
	 * In case a new image is created then the old one has decrementReferenceCounter() called.
	 * @param size
	 * @param componentOrder
	 * @param allocator
	 * @return
	 */
	public NativeImage getImage(SizeInt size, ENativeImageComponentOrder componentOrder, INativeMemoryAllocator allocator)
	{
		if(im!=null &&
				(!im.getSize().equals(size) 
				|| !im.getComponentOrder().equals(componentOrder) )
				)
		{
			NativeImage prev=im;
			im=null;
			prev.decrementReferenceCounter();
		}
		if(im == null)
		{
			im=NativeImage.create(size, componentOrder, allocator);
		}
		return im;
	}
	/**
	 * Get the cached image instance.
	 * @return may be null in case it is not created yet.
	 */
	public NativeImage getCurrentImage()
	{
		return im;
	}
	private boolean disposed=false;
	@Override
	public void dispose() {
		disposed=true;
		if(im!=null)
		{
			im.decrementReferenceCounter();
			im=null;
		}
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}
}
