#ifndef NATIVE_LIB_LIBSCHRIFT_H
#define NATIVE_LIB_LIBSCHRIFT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif


/**
 * C representation of Java TrueTypeFont object
 */
typedef struct {
    const char* fontFamily;
    float fontSize;
    double letterSpacing;
    bool bold;
    bool italic;
    bool underline;
} T_TrueTypeFont;


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
uint64_t qls_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h);

/**
 * Disposes the surface instance allocated earlier with
 * {@link #createSurfaceWithData(ByteBuffer, int, int)}.
 * 
 * @param surfaceHandle The surface to dispose
 */
void qls_disposeSurfacePrivate(uint64_t surfaceHandle);

/**
 * Renders text onto a surface
 * 
 * @param surfaceHandle The surface handle returned by {@link #createSurfaceWithData(ByteBuffer, int, int)}.
 * @param font The font parameters
 * @param text Text to render as UTF-16 character array (as JNI->GetStringChars returns)
 * @param textLen The number of UTF-16 chars in text
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
T_SizeInt qls_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text, uint32_t textLen,  
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode);

/**
 * Calculates the layout of text without rendering it
 * 
 * @param font The font parameters
 * @param text Text to calculate layout for, specified as UTF-16 character array (as JNI->GetStringChars returns)
 * @param hAlign Horizontal alignment
 * @param vAlign Vertical alignment
 * @param width The maximal width of the bounding box within surface
 * @param height The maximal height of the bounding box within surface
 * @param wrapMode How to wrap long texts amongst white spaces
 * 
 * @return The bounding box calculated during laying out the text
 */
T_SizeInt qls_layoutTextPrivate(T_TrueTypeFont* font, const uint16_t* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode);

#ifdef __cplusplus
}
#endif

#endif /* NATIVE_LIB_LIBSCHRIFT_H */
