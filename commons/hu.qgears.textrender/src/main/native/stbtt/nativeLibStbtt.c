#include "nativeLibStbtt.h"
#include <stdio.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>

#include "qstb_glyphreader.h"
#include "qstb_line.h"

// Structure to represent surface data
typedef struct {
    uint8_t* data;
    int32_t width;
    int32_t height;
    int32_t stride;
} T_SurfaceData;

static T_SurfaceData* qstb_get_surfacedata(uint64_t id);

uint64_t qstb_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h)
{
    // Allocate memory for surface data structure
    T_SurfaceData* surfaceData = (T_SurfaceData*)malloc(sizeof(T_SurfaceData));
    if (surfaceData == NULL) {
        return 0; // Return 0 on allocation failure
    }
    
    // Initialize the surface data
    surfaceData->data = data;
    surfaceData->width = w;
    surfaceData->height = h;
    surfaceData->stride = w;
    // surfaceData->stride = (w + 3) & ~3;
    
    // Return the memory address as the handle
    return (uint64_t)surfaceData;
}

T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode);
// {
//     T_SurfaceData* surface = qstb_get_surfacedata(surfaceHandle);
//     if (surface) {
//         uint32_t* surfaceData = (uint32_t*)surface->data;
//         if (surfaceData)
//         {
//             // Dummy implementation - draw some "random" lines
//             uint32_t i = 0;
//             uint32_t j = 0;
//             for (j = x; j < width; j++ ) {
//                 surfaceData[i*width + j] = 0xFF0000FF;//RED
//                 i = ((i+1) % height);
//             }
//         }
//
//         T_SizeInt result = {100, 50}; // dummy values
//         return result;
//     }
// }

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode);
// {
//     // Stub implementation - to be filled later
//     T_SizeInt result = {100, 50}; // dummy values
//     return result;
// }

void qstb_disposeSurfacePrivate(uint64_t surfaceHandle)
{
    // Check if handle is valid
    if (surfaceHandle == 0) {
        return;
    }
    
    // Cast handle back to T_SurfaceData pointer and free it
    T_SurfaceData* surfaceData = (T_SurfaceData*)surfaceHandle;
    free(surfaceData);
}

static T_SurfaceData* qstb_get_surfacedata(uint64_t id) {
    // Cast the handle back to T_SurfaceData pointer
    return (T_SurfaceData*)id;
}

//----------------------------------------------------------------------------

// static bool get_font_file(T_TrueTypeFont *font, char *filePath, uint32_t filePathLength) {
//     bool retSuccess = true;
//     FcInit();
//
//     FcPattern *pat = FcPatternCreate();
//
//     FcPatternAddString(pat, FC_FAMILY, (FcChar8 *) font->fontFamily);
//     FcPatternAddInteger(pat, FC_WEIGHT, font->bold ? FC_WEIGHT_BOLD : FC_WEIGHT_NORMAL);
//     FcPatternAddInteger(pat, FC_SLANT, font->italic ? FC_SLANT_ITALIC : FC_SLANT_ROMAN);
//
//     FcConfigSubstitute(NULL, pat, FcMatchPattern);
//     FcDefaultSubstitute(pat);
//
//     FcResult result;
//     FcPattern *fc_font = FcFontMatch(NULL, pat, &result);
//
//     if (fc_font) {
//         char *file;
//         int index;
//
//         if (FcPatternGetString(fc_font, FC_FILE, 0, (FcChar8 **) &file) == FcResultMatch) {
//             uint32_t fLen = (uint32_t) strlen(file);
//             if (fLen < filePathLength - 1) {
//                 memcpy(filePath, file, fLen);
//                 filePath[fLen] = '\0';
//                 LOG("Font file: %s\n", filePath);
//             } else {
//                 // ERROR(eh, QLS_ERROR_LONG_FONT_PATH, "File path for font %s too long : %d", font->fontFamily, fLen);
//                 retSuccess = false;
//             }
//         }
//         FcPatternDestroy(fc_font);
//     } else {
//         // ERROR(eh, QLS_ERROR_MISSING_FONT, "No matching font found %s.", font->fontFamily);
//         retSuccess = false;
//     }
//
//     return retSuccess;
// }

static void qstb_InitFont(T_TrueTypeFont* font) {
    if (font->stb.inited) {
        return;
    }

    const char* const pathFont = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf";
    FILE* const fFont = fopen(pathFont, "rb");
    assert(fFont != NULL);
    static uint8_t bufFont[10 * 1024 * 1024] = {0};
    memset(bufFont, 0, sizeof(bufFont));
    size_t zuRet = fread(bufFont, sizeof(bufFont), 1, fFont);
    assert(zuRet == 1);
    fclose(fFont);

    stbtt_InitFont(&font->stb.font, bufFont, 0);

    stbtt_GetFontVMetrics(&font->stb.font, &font->stb.ascent, &font->stb.descent, &font->stb.lineGap);

    stbtt_GetFontBoundingBox(&font->stb.font, &font->stb.x0, &font->stb.y0, &font->stb.x1, &font->stb.y1);

    font->stb.scale = stbtt_ScaleForPixelHeight(&font->stb.font, font->fontSize);

    font->stb.inited = true;
}

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{
    qstb_InitFont(font);

    struct glyphreader reader = glyphreader_init(text);
    struct line line = line_init(reader.off);

    size_t nLines = 0;
    int32_t wLineMax = 0;
    do {
        nLines += 1;
        if (wLineMax < line.w) {
            wLineMax = line.w;
        }

        // reader = reader.seek(line.end);
        reader = glyphreader_seek(reader, line.end);
        line = line_next(font, reader ,width, font->stb.scale, wrapMode);
    } while (line.end < reader.len);

    return (T_SizeInt) {
        .width = wLineMax * font->stb.scale, //TODO rounding, LSB?
        .height = (font->stb.ascent - font->stb.descent + font->stb.lineGap) * nLines * font->stb.scale //TODO last lineGap?
    };
}

#define MIN(a, b) (((a) < (b)) ? (a) : (b))
#define MAX(a, b) (((a) < (b)) ? (b) : (a))
#define CLAMP(a, b, c) (MIN(MAX((a), (b)), (c)))

static void qstb_MemBlend4(T_SurfaceData*, const uint8_t*, int32_t, int32_t, int32_t, int32_t, int32_t, int32_t, int32_t, bool
    , uint8_t, uint8_t, uint8_t, uint8_t);
T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
{
    qstb_InitFont(font);

    T_SurfaceData* surface = qstb_get_surfacedata(surfaceHandle);
    if (!surface) {
        //TODO
        return (T_SizeInt) { 0, 0 };
    }
    if (!surface->data) {
        //TODO
        return (T_SizeInt) { 0, 0 };
    }

    struct glyphreader reader = glyphreader_init(text);
    enum { WITHOUT, WITH, WHOLE };
    struct line line[3] = {0};

    line[WITHOUT] = line_peek(font, reader, width, font->stb.scale, wrapMode);
    line[WITH] = line[WITHOUT];
    line[WHOLE] = line_peek(font, reader, width, font->stb.scale, wrapMode);

    static uint8_t tmpbuffer[256 * 256] = {0};
    memset(tmpbuffer, 0, sizeof(tmpbuffer));

    struct {
        uint8_t* p;
        int32_t w, h, stride;
        int32_t memSize;
    } tmp = {0};
    tmp.w = (int)((font->stb.x1 - font->stb.x0) * font->stb.scale) + 2;
    tmp.h = (int)((font->stb.y1 - font->stb.y0) * font->stb.scale) + 2;
    if (tmp.w <= 256 && tmp.h <= 256) {
        tmp.memSize = 256 * 256;
        tmp.p = tmpbuffer;
        tmp.stride = 256;
    } else {
        tmp.memSize = tmp.w * tmp.h;
        tmp.p = calloc(tmp.memSize, sizeof(uint8_t));
        tmp.stride = tmp.w;
    }

    size_t iLine = 0;

    while (line[WITHOUT].end < line[WHOLE].end) {
        reader = glyphreader_seek(reader, line[WITHOUT].end);
        line[WITH] = line_extend(font, line[WITHOUT], reader);

        const char32_t lastPrintable = line[WITHOUT].lastPrintable;
        const char32_t codepoint = line[WITH].lastPrintable;
        const int32_t wSpace = line[WITHOUT].wSpace;
        const int32_t kernAdvance = stbtt_GetCodepointKernAdvance(&font->stb.font, lastPrintable, codepoint);

        int32_t leftSideBearing = 0;
        stbtt_GetCodepointHMetrics(&font->stb.font, codepoint, NULL, &leftSideBearing);

        const int32_t relativeUnscaledX = wSpace + kernAdvance + leftSideBearing;
        const float shift_x = fmodf(relativeUnscaledX * font->stb.scale, 1.0f);

        const int32_t relativeUnscaledY = iLine * (font->stb.ascent - font->stb.descent + font->stb.lineGap) + font->stb.ascent;
        const float shift_y = fmodf(relativeUnscaledY * font->stb.scale, 1.0f);

        { //render & blend
            int32_t ix0, iy0, ix1, iy1;
            stbtt_GetCodepointBitmapBoxSubpixel(&font->stb.font, codepoint, font->stb.scale, font->stb.scale
                    , shift_x, shift_y, &ix0, &iy0, &ix1, &iy1);

            memset(tmp.p, 0, tmp.memSize);
            stbtt_MakeCodepointBitmapSubpixel(&font->stb.font, tmp.p, tmp.w, tmp.h, tmp.stride
                    , font->stb.scale, font->stb.scale, shift_x, shift_y, codepoint);

            int32_t targetX = x + (int32_t)(relativeUnscaledX * font->stb.scale) - ix0;
            int32_t targetY = y + (int32_t)(relativeUnscaledY * font->stb.scale) - iy0;
            qstb_MemBlend4(surface, tmp.p, tmp.w, tmp.h, tmp.stride, targetX, targetY, width, height, clip
                   , CLAMP(0.0f, r, 1.0f) * 0xFFu
                   , CLAMP(0.0f, g, 1.0f) * 0xFFu
                   , CLAMP(0.0f, b, 1.0f) * 0xFFu
                   , CLAMP(0.0f, a, 1.0f) * 0xFFu);
        }

        line[WITHOUT] = line[WITH];
    }

    if (tmp.p != tmpbuffer) {
        free(tmp.p);
    }

    return qstb_layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode);
}

static void qstb_MemBlend4(T_SurfaceData* surface, const uint8_t* pin, int32_t win, int32_t hin, int32_t sin
    , int32_t x, int32_t y, int32_t width, int32_t height, bool clip
    , uint8_t r, uint8_t g, uint8_t b, uint8_t a)
{
    int32_t xei = MIN(win, surface->width);
    if (clip) {
        xei = MIN(xei, width);
    }

    int32_t yei = MIN(hin, surface->height);
    if (clip) {
        yei = MIN(yei, height);
    }

    int32_t x0i = 0;
    int32_t y0i = 0;
    if (x < 0) {
        x0i -= x;
        x = 0;
    }
    if (y < 0) {
        y0i -= y;
        y = 0;
    }

    for (int32_t y = y0i; y < yei; y++) {
        for (int32_t x = x0i; x < xei; x++) {
            uint8_t bin = CLAMP(0u, (unsigned) pin[y * sin + x] * a / 0xFFu, 0xFFu);

            uint8_t* pout = surface->data + 4 * (y * surface->stride + x);

            enum { B, G, R, A };
            if (bin != 0) {
                r = CLAMP(0u, (unsigned) r * (0xFFu - bin) / 0xFFu, 0xFFu);
                g = CLAMP(0u, (unsigned) g * (0xFFu - bin) / 0xFFu, 0xFFu);
                b = CLAMP(0u, (unsigned) b * (0xFFu - bin) / 0xFFu, 0xFFu);

                // premultiplied
                pout[R] = CLAMP(0u, r + (unsigned) pout[R] * (0xFFu - bin) / 0xFFu, 0xFFu);
                pout[G] = CLAMP(0u, g + (unsigned) pout[G] * (0xFFu - bin) / 0xFFu, 0xFFu);
                pout[B] = CLAMP(0u, b + (unsigned) pout[B] * (0xFFu - bin) / 0xFFu, 0xFFu);
                pout[A] = CLAMP(0u, bin + (unsigned) pout[A] * (0xFFu - bin) / 0xFFu, 0xFFu);
            }
        }
    }
}