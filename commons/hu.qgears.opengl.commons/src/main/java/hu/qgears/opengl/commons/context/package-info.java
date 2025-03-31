/**
 * OpenGL state hase problems:
 *  * when an object changes a state it should reset that to the
 *    original state
 *  * the object does not know the original state
 *  * querying the original state is a slow operation
 *    (on Linux with current NVidia driver)
 * 
 * This class manages OpenGL states. This must be used to set
 * every aspect of the opengl context.
 * 
 */
package hu.qgears.opengl.commons.context;

