#ifndef NATIVE_LIB_STBTT_H
#define NATIVE_LIB_STBTT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * C representation of Java SizeInt object
 */
typedef struct {
    int32_t width;
    int32_t height;
} T_SizeInt;

/**
 * Creates a render surface. Assumptions :
 * 
 * * RGBA pixel representation. * Size of data equals w * h * 4
 * 
 * @param data Pointer to the surface data buffer
 * @param w Width of the surface
 * @param h Height of the surface
 * 
 * @return The surface id (handle) that identifies this instance.
 */
uint64_t qstb_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h);

/**
 * Disposes the surface instance allocated earlier with
 * {@link #createSurfaceWithData(ByteBuffer, int, int)}.
 * 
 * @param surfaceHandle The surface to dispose
 */
void qstb_disposeSurfacePrivate(uint64_t surfaceHandle);

/**
 * Renders text onto a surface
 * 
 * @param surfaceHandle The surface handle returned by {@link #createSurfaceWithData(ByteBuffer, int, int)}.
 * @param fontFamily Name of the font family to use
 * @param text Text to render
 * @param hAlign Horizontal alignment
 * @param vAlign Vertical alignment  
 * @param x X coordinate of top left corner
 * @param y Y coordinate of top left corner
 * @param width The maximal width of the bounding box within surface
 * @param height The maximal height of the bounding box within surface
 * @param r Red color component (0.0 to 1.0)
 * @param g Green color component (0.0 to 1.0)
 * @param b Blue color component (0.0 to 1.0)
 * @param a Alpha channel of the color (0.0 to 1.0)
 * @param clip Clip text if it does not fit into specified area
 * @param wrapMode How to wrap long texts amongst white spaces
 * 
 * @return The bounding box calculated during laying out the text
 */
T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode);

/**
 * Calculates the layout of text without rendering it
 * 
 * @param fontFamily Name of the font family to use
 * @param text Text to calculate layout for
 * @param hAlign Horizontal alignment
 * @param vAlign Vertical alignment
 * @param width The maximal width of the bounding box within surface
 * @param height The maximal height of the bounding box within surface
 * @param wrapMode How to wrap long texts amongst white spaces
 * 
 * @return The bounding box calculated during laying out the text
 */
T_SizeInt qstb_layoutTextPrivate(const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode);

#ifdef __cplusplus
}
#endif

#endif /* NATIVE_LIB_STBTT_H */