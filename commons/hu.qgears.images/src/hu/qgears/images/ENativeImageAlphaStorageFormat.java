package hu.qgears.images;

/**
 * Alpha storage format of a native image.
 * @author rizsi
 *
 */
public enum ENativeImageAlphaStorageFormat {
	/**
	 * Normal torage format (like libart)
	 */
	normal,
	/**
	 * Premultiplied alpha format (like Cairo)
	 */
	premultiplied,
}
