package hu.qgears.images.vnc;

import hu.qgears.images.NativeImage;

/*
 * Snapshot of the VNC client.
 * Sonar warning suppression: this is a simple DTO class with no actual benefit
 * of adding getters and setters.
 */
@SuppressWarnings("squid:ClassVariableVisibilityCheck")
public class VncSnapShot implements AutoCloseable {
	private VNCClient client;
	/**
	 * Constructor should only be called by the VNC client implementation.
	 * @param client
	 * @param image
	 * @param changeCounter
	 */
	public VncSnapShot(VNCClient client, NativeImage image, int changeCounter) {
		super();
		this.client=client;
		this.image = image;
		this.changeCounter = changeCounter;
	}
	/**
	 * This is the snapshot image of the VNC client. Its data will not be updated
	 * (by the VNC client implementation) until this snapshot object is closed.
	 * 
	 * The image reference may be null until the client did not receive the first image
	 * from the VNC server so the code using this class must check it for null.
	 * The snapshot object must be freed even in case the image reference is null.
	 * 
	 * The image object itself must not be reference counter decremented
	 * by the user of this API.
	 */
	public NativeImage image;
	/**
	 * The change counter that represents this state of the VNC client
	 * (the image wil be the same until this change counter is read from the client)
	 */
	public int changeCounter;
	/**
	 * Free this snapshot. The image is not locked any longer, VNC client can update
	 * it.
	 */
	@Override
	public void close() {
		client.released();
	}
}
