#ifndef NATIVE_LIB_STBTT_H
#define NATIVE_LIB_STBTT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// C representation of Java SizeInt object
typedef struct {
    int32_t width;
    int32_t height;
} T_SizeInt;

/*
 * Method:    createSurfaceWithDataPrivate
 * Signature: (Ljava/nio/ByteBuffer;II)J
 */
uint64_t qstb_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h);

/*
 * Method:    renderTextPrivate
 * Signature: (JLjava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IIIIFFFFZLhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode);

/*
 * Method:    layoutTextPrivate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IILhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
T_SizeInt qstb_layoutTextPrivate(const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode);

/*
 * Method:    disposeSurfacePrivate
 * Signature: (J)V
 */
void qstb_disposeSurfacePrivate(uint64_t surfaceHandle);

#ifdef __cplusplus
}
#endif

#endif /* NATIVE_LIB_STBTT_H */
