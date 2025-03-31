package hu.qgears.opengl.commons;

/**
 * Modes of the used rendering technique.
 * 
 * All are implemented (for at least some objects) so 
 * their performance can be compared to each other.
 * 
 * @author rizsi
 *
 */
public enum RenderMode {
	list,
	directCommand,
	vertexBuffer,
}
