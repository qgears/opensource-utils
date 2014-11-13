/**
 * DevIL package integration into OpenGL commons:
 *  * DevIL Loads many types of images and performs better than java.awt.ImageIO
 *  * Images must be loaded on a DevIL worker thread:
 *   * DevIL must be accessed on a single thread :-(
 *  * Loaded image must be copied to a NativeImage because:
 *   * DevIL must handle the allocated memory itself
 *   * DevIL can not load image with storage alignment required
 */
package hu.qgears.images.devil;

