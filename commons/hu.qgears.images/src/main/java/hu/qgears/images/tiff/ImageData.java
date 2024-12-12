package hu.qgears.images.tiff;

import hu.qgears.commons.IDisposeable;

/**
 * Describes the data necessary for loading a tiff image. 
 * 
 * @author agostoni
 *
 */
public class ImageData extends ImageDataConnector implements IDisposeable{

	private boolean disposed = false;
	
	public ImageData() {
		init();
	}
	
	@Override
	public void dispose() {
		if (!isDisposed()){
			disposePrimitive();
			disposed = true;
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

}
