#include "nativeLibStbtt.h"
#include <stdio.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include <fontconfig/fontconfig.h>

#include "qstb_glyphreader.h"
#include "qstb_line.h"

// Structure to represent surface data
typedef struct {
    uint8_t* data;
    int32_t width;
    int32_t height;
    int32_t stride;
    int32_t pixelSize;
} T_SurfaceData;

static T_SurfaceData* qstb_get_surfacedata(uint64_t id);

uint64_t qstb_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h, int32_t pixelformat)
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
    switch (pixelformat) {
        case ENICO_BGRA: {
            surfaceData->stride = w;
            surfaceData->pixelSize = 4;
            break;
        }
        default:
        case ENICO_ALPHA: {
            surfaceData->stride = (w + 3) & ~3;
            surfaceData->pixelSize = 1;
            break;
        }
    }

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

static bool get_font_file(const T_TrueTypeFont *font, char *filePath, uint32_t filePathLength) {
    bool retSuccess = true;
    FcInit();

    FcPattern *pat = FcPatternCreate();

    FcPatternAddString(pat, FC_FAMILY, (FcChar8 *) font->fontFamily);
    FcPatternAddInteger(pat, FC_WEIGHT, font->bold ? FC_WEIGHT_BOLD : FC_WEIGHT_NORMAL);
    FcPatternAddInteger(pat, FC_SLANT, font->italic ? FC_SLANT_ITALIC : FC_SLANT_ROMAN);

    FcConfigSubstitute(NULL, pat, FcMatchPattern);
    FcDefaultSubstitute(pat);

    FcResult result;
    FcPattern *fc_font = FcFontMatch(NULL, pat, &result);

    if (fc_font) {
        char *file;
        // int index;

        if (FcPatternGetString(fc_font, FC_FILE, 0, (FcChar8 **) &file) == FcResultMatch) {
            uint32_t fLen = (uint32_t) strlen(file);
            if (fLen < filePathLength - 1) {
                memcpy(filePath, file, fLen);
                filePath[fLen] = '\0';
                // LOG("Font file: %s\n", filePath);
            } else {
                // ERROR(eh, QLS_ERROR_LONG_FONT_PATH, "File path for font %s too long : %d", font->fontFamily, fLen);
                retSuccess = false;
            }
        }
        FcPatternDestroy(fc_font);
    } else {
        // ERROR(eh, QLS_ERROR_MISSING_FONT, "No matching font found %s.", font->fontFamily);
        retSuccess = false;
    }

    return retSuccess;
}

static void qstb_InitFont(T_TrueTypeFont* font) {
    if (font->stb.inited) {
        return;
    }

    // const char* const pathFont = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf";
    static char pathFont[256] = {0};
    memset(pathFont, 0, sizeof(pathFont));
    bool bRet = get_font_file(font, pathFont, sizeof(pathFont));
    assert(bRet);

    FILE* const fFont = fopen(pathFont, "rb");
    assert(fFont != NULL);
    static uint8_t bufFont[10 * 1024 * 1024] = {0};
    memset(bufFont, 0, sizeof(bufFont));
    size_t zuRet = fread(bufFont, 1, sizeof(bufFont), fFont);
    // assert(zuRet == 1);
    (void) zuRet;
    fclose(fFont);

    stbtt_InitFont(&font->stb.font, bufFont, 0);

    stbtt_GetFontVMetrics(&font->stb.font, &font->stb.ascent, &font->stb.descent, &font->stb.lineGap);

    stbtt_GetFontBoundingBox(&font->stb.font, &font->stb.x0, &font->stb.y0, &font->stb.x1, &font->stb.y1);

    font->stb.scale = stbtt_ScaleForPixelHeight(&font->stb.font, font->fontSize);

    font->stb.inited = true;

    font->stb.scale *= (double) abs(font->stb.y1 - font->stb.y0) / abs(font->stb.ascent - font->stb.descent);
    //Is this how Cairo calculates the scale?
    //seems silly...
    //TODO consider a flat 393/345
}

T_SizeInt qstb_layoutTextPrivateTopLeft(T_TrueTypeFont* font, const uint16_t* text,
                            int32_t width, int32_t height, uint32_t wrapMode)
{
    qstb_InitFont(font);

    struct glyphreader reader = glyphreader_init(text);
    struct line line = line_peek(font, reader, width, font->stb.scale, wrapMode);

    struct line lineBefore;
    size_t nLines = 0;
    int32_t wLineMax = 0;
    do {
        nLines += 1;
        if (wLineMax < line.w) {
            wLineMax = line.w;
        }

        // reader = reader.seek(line.end);
        reader = glyphreader_seek(reader, line.end);
        lineBefore = line;
        line = line_next(font, reader, width, font->stb.scale, wrapMode);
    } while (lineBefore.end != line.end);

    return (T_SizeInt) {
        .width = wLineMax * font->stb.scale, //TODO rounding, LSB?
        .height = (font->stb.ascent - font->stb.descent + font->stb.lineGap) * nLines * font->stb.scale //TODO last lineGap?
    };
}

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{
    return qstb_layoutTextPrivateTopLeft(font, text, width, height, wrapMode);
}

#define MIN(a, b) (((a) < (b)) ? (a) : (b))
#define MAX(a, b) (((a) < (b)) ? (b) : (a))
#define CLAMP(a, b, c) (MIN(MAX((a), (b)), (c)))

static void qstb_MemBlend4(T_SurfaceData* surface, const T_SurfaceData* in
    , int32_t px, int32_t py
    , int32_t px0, int32_t py0, int32_t width, int32_t height, bool clip
    , uint8_t pr, uint8_t pg, uint8_t pb, uint8_t pa);
T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
{
    qstb_InitFont(font);
    int32_t vAlignY;
    T_SurfaceData* surface;
    { //init vars
        enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM };

        switch (vAlign) {
            default:
            case VALIGN_TOP: {
                vAlignY = 0;
                break;
            }
            case VALIGN_MIDDLE: {
                int32_t textHeight = qstb_layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode).height;
                vAlignY = (height - textHeight) / 2;
                break;
            }
            case VALIGN_BOTTOM: {
                int32_t textHeight = qstb_layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode).height;
                vAlignY = height - textHeight;
                break;
            }
        }

        surface = qstb_get_surfacedata(surfaceHandle);
        if (!surface) {
            //TODO
            return (T_SizeInt) { 0, 0 };
        }
        if (!surface->data) {
            //TODO
            return (T_SizeInt) { 0, 0 };
        }
    }

    struct glyphreader reader = glyphreader_init(text);
    enum { WITHOUT, WITH, WHOLE };
    struct line line[3] = {0};

    line[WITHOUT] = line_init(reader.off);
    line[WITH] = line[WITHOUT];
    line[WHOLE] = line_peek(font, reader, width, font->stb.scale, wrapMode);

    enum { W_TEMP_BUFFER = 256, H_TEMP_BUFFER = 256 };
    static uint8_t tmpbuffer[W_TEMP_BUFFER * H_TEMP_BUFFER] = {0};
    memset(tmpbuffer, 0, sizeof(tmpbuffer));

    T_SurfaceData tmp = {0};
    tmp.pixelSize = 1;
    tmp.width = (int)((font->stb.x1 - font->stb.x0) * font->stb.scale) + 2;
    tmp.height = (int)((font->stb.y1 - font->stb.y0) * font->stb.scale) + 2;
    if (tmp.width <= W_TEMP_BUFFER && tmp.height <= H_TEMP_BUFFER) {
        tmp.data = tmpbuffer;
        tmp.stride = W_TEMP_BUFFER;
    } else {
        tmp.stride = tmp.width;
        tmp.data = calloc(tmp.height * tmp.stride, sizeof(uint8_t));
    }

    size_t iLine = 0;
    struct line lineBefore;

    do {
        int32_t hAlignX;
        enum { HALIGN_LEFT, HALIGN_CENTER, HALIGN_RIGHT, HALIGN_JUSTIFY };
        switch (hAlign) {
            default:
            case HALIGN_LEFT: {
                hAlignX = 0;
                break;
            }
            case HALIGN_CENTER: {
                hAlignX = (width - (int32_t)(line[WHOLE].w * font->stb.scale)) / 2;
                break;
            }
            case HALIGN_RIGHT: {
                hAlignX = width - (int32_t)(line[WHOLE].w * font->stb.scale);
            }
            //TODO justify
        }

        while (line[WITHOUT].end < line[WHOLE].end) {
            reader = glyphreader_seek(reader, line[WITHOUT].end);
            line[WITH] = line_extend(font, line[WITHOUT], reader);

            const char32_t lastPrintable = line[WITHOUT].lastPrintable;
            const char32_t codepoint = line[WITH].lastPrintable;
            const int32_t wSpace = line[WITHOUT].wSpace;
            //TODO is \0 handling guaranteed?
            const int32_t kernAdvance = stbtt_GetCodepointKernAdvance(&font->stb.font, lastPrintable, codepoint);

            // int32_t leftSideBearing = 0;
            // stbtt_GetCodepointHMetrics(&font->stb.font, codepoint, NULL, &leftSideBearing);

            const int32_t relativeUnscaledX = wSpace /*- leftSideBearing*/ + kernAdvance;
            const float shift_x = fmodf(relativeUnscaledX * font->stb.scale, 1.0f);

            const int32_t relativeUnscaledY = iLine * (font->stb.ascent - font->stb.descent + font->stb.lineGap) + font->stb.ascent;
            const float shift_y = fmodf(relativeUnscaledY * font->stb.scale, 1.0f);

            if (is_graph(codepoint)) { //render & blend
                int32_t ix0, iy0, ix1, iy1;
                stbtt_GetCodepointBitmapBoxSubpixel(&font->stb.font, codepoint, font->stb.scale, font->stb.scale
                        , shift_x, shift_y, &ix0, &iy0, &ix1, &iy1);

                memset(tmp.data, 0, tmp.height * tmp.stride);
                stbtt_MakeCodepointBitmapSubpixel(&font->stb.font, tmp.data, tmp.width, tmp.height, tmp.stride
                        , font->stb.scale, font->stb.scale, shift_x, shift_y, codepoint);

                int32_t targetX = hAlignX + x + ((int32_t)(relativeUnscaledX * font->stb.scale) /*+ 1*/) + ix0;
                //TODO line-first character x?
                int32_t targetY = vAlignY + y + (int32_t)(relativeUnscaledY * font->stb.scale) + iy0;
                qstb_MemBlend4(surface, &tmp, targetX, targetY, x, y, width, height, clip
                       , CLAMP(0.0f, r, 1.0f) * 0xFFu
                       , CLAMP(0.0f, g, 1.0f) * 0xFFu
                       , CLAMP(0.0f, b, 1.0f) * 0xFFu
                       , CLAMP(0.0f, a, 1.0f) * 0xFFu);
            }

            line[WITHOUT] = line[WITH];
        }


        iLine += 1;
        lineBefore = line[WHOLE];
        reader = glyphreader_seek(reader, line[WHOLE].end);
        line[WHOLE] = line_next(font, reader, width, font->stb.scale, wrapMode);
        line[WITHOUT] = line_init(line[WHOLE].off);
        line[WITH] = line[WITHOUT];
        reader = glyphreader_seek(reader, line[WHOLE].off);
    } while (lineBefore.end < line[WHOLE].end);


    if (tmp.data != tmpbuffer) {
        free(tmp.data);
    }

    return qstb_layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode);
}

static void qstb_MemBlend4(T_SurfaceData* surface, const T_SurfaceData* in
    , int32_t px, int32_t py
    , int32_t px0, int32_t py0, int32_t width, int32_t height, bool clip
    , const uint8_t pr, const uint8_t pg, const uint8_t pb, const uint8_t pa)
{
    int32_t xei = MIN(in->width, surface->width - px);
    if (clip) {
        xei = MIN(xei, (width + px0) - px);
    }

    int32_t yei = MIN(in->height, surface->height - py);
    if (clip) {
        yei = MIN(yei, (height + py0) - py);
    }

    int32_t x0i = 0;
    int32_t y0i = 0;
    if (px < 0) {
        x0i -= px;
    }
    if (py < 0) {
        y0i -= py;
    }

    for (int32_t y = y0i; y < yei; y++) {
        for (int32_t x = x0i; x < xei; x++) {
            uint8_t bin = CLAMP(0u, (unsigned) in->data[y * in->stride + x] * pa / 0xFFu, 0xFFu);

            switch (surface->pixelSize) {
                case 4: {
                    uint8_t* pout = surface->data + 4 * ((y + py) * surface->stride + (x + px));

                    enum { B, G, R, A };
                    if (bin != 0) {
                        uint8_t r = CLAMP(0u, (unsigned) pr * bin / 0xFFu, 0xFFu);
                        uint8_t g = CLAMP(0u, (unsigned) pg * bin / 0xFFu, 0xFFu);
                        uint8_t b = CLAMP(0u, (unsigned) pb * bin / 0xFFu, 0xFFu);

                        // premultiplied
                        pout[R] = CLAMP(0u, r + (unsigned) pout[R] * (0xFFu - bin) / 0xFFu, 0xFFu);
                        pout[G] = CLAMP(0u, g + (unsigned) pout[G] * (0xFFu - bin) / 0xFFu, 0xFFu);
                        pout[B] = CLAMP(0u, b + (unsigned) pout[B] * (0xFFu - bin) / 0xFFu, 0xFFu);
                        pout[A] = CLAMP(0u, bin + (unsigned) pout[A] * (0xFFu - bin) / 0xFFu, 0xFFu);
                    }
                    break;
                }
                default:
                case 1: {
                    uint8_t* pout = surface->data + ((y + py) * surface->stride + (x + px));

                    if (bin != 0) {
                        pout[0] = CLAMP(0u, bin + (unsigned) pout[0] * (0xFFu - bin) / 0xFFu, 0xFFu);
                    }
                    break;
                }
            }
        }
    }
}