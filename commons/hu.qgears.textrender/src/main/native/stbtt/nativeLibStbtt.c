#include "nativeLibStbtt.h"

#define STB_TRUETYPE_IMPLEMENTATION
#define STBTT_STATIC
#include "stb_truetype.h"

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <uchar.h>

// Structure to represent surface data
typedef struct {
    uint8_t* data;
    int32_t width;
    int32_t height;
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
    
    // Return the memory address as the handle
    return (uint64_t)surfaceData;
}

T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode);

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode);

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

//--

static struct T_TrueTypeFont2 {
    stbtt_fontinfo font; //font.data is dynamically allocated
    const T_TrueTypeFont* pSource;
    int32_t ascent, descent, lineGap;
    int32_t x0, y0, x1, y1;
    float scale;
};

static void qstb_InitFont(const T_TrueTypeFont* const source, struct T_TrueTypeFont2* const result) {
    assert(result != NULL);
    result->pSource = source;
    { //stbtt_InitFont
        FILE* fFont = fopen("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", "rb"); //TODO find the font file
        assert(fFont != NULL); // no error
        int ret = fseek(fFont, 0 , SEEK_END);
        assert(ret == 0); // no error
        const long size = ftell(fFont);
        ret = fseek(fFont, 0, SEEK_SET);
        assert(ret == 0); // no error
        uint8_t* bufFont = malloc(size);
        assert(bufFont != NULL); // no error
        size_t ret2 = fread(bufFont, size, 1, fFont);
        assert(ret2 == 1); // no error
        ret = fclose(fFont);
        assert(ret == 0);
        ret = stbtt_InitFont(&result->font, bufFont, 0);
        assert(ret != 0); // no error
        assert(result->font.data == bufFont); // can free later
    }
    stbtt_GetFontVMetrics(&result->font, &result->ascent, &result->descent, &result->lineGap);
    assert(0 <= result->ascent);
    assert(result->descent <= 0);
    stbtt_GetFontBoundingBox(&result->font, &result->x0, &result->y0, &result->x1, &result->y1);
    assert(result->x0 <= result->x1);
    assert(result->y0 <= result->y1);
    // assert(result->ascent <= -result->y0);
    result->scale = stbtt_ScaleForPixelHeight(&result->font, source->fontSize);
    assert(0.0f <= result->scale);
}

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const char* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{
    const size_t len = strlen(text);
    if (len == 0) {
        /**
         *  TODO semantics
         *  Should the height be set anyway?
         **/
        return (T_SizeInt) {0, 0};
    }

    assert(len != 0);
    struct T_TrueTypeFont2 f;
    qstb_InitFont(font, &f); //TODO cache

    float cx = 0.0f;
    struct {
        int32_t x0, y0, x1, y1, advanceWidth;
    } box = {};
    for (int32_t i = 0; i < len; i++) {
        const char32_t c = text[i]; //TODO mbrtoc32
        memset(&box, 0x00, sizeof(box));
        stbtt_GetCodepointHMetrics(&f.font, c, &box.advanceWidth, NULL);
        stbtt_GetCodepointBitmapBoxSubpixel(&f.font, c, f.scale, f.scale, (cx - floorf(cx)), 0.0f
            , &box.x0, &box.y0, &box.x1, &box.y1);
        if (i == 0) {
            cx += -box.x0;
        }
        if (i < len - 1) {
            cx += f.scale * box.advanceWidth;
            cx += f.scale * stbtt_GetCodepointKernAdvance(&f.font, c, text[i+1]);
        }
    }

    //TODO linebreaks
    //TODO should the result reflect exceptionally high/low characters? Should it be snug heightwise?
    return (T_SizeInt) { floorf(cx) + box.x1, f.scale * (f.ascent - f.descent) };
}

static int32_t max(const int32_t a, const int32_t b) {
    return a < b ? b : a;
}
static int32_t min(const int32_t a, const int32_t b) {
    return a < b ? a : b;
}

static void qstb_MemBlend(
    const T_SurfaceData* const surface
    , const int32_t px0o, const int32_t py0o
    , const uint8_t* const pp
    , const int32_t pw, const int32_t ph
    , const float pr, const float pg, const float pb, const float pa)
{
    const int32_t x0o = max(0, px0o);
    const int32_t y0o = max(0, py0o);
    const int32_t xendo = min(px0o + pw, surface->width);
    const int32_t yendo = min(py0o + ph, surface->height);
    const int32_t x0i = 0 + (x0o - px0o);
    const int32_t y0i = 0 + (y0o - py0o);

    enum { R, G, B, A };
    for (int32_t yo = y0o, yi = y0i; yo < yendo; yo++, yi++) {
        for (int32_t xo = x0o, xi = x0i; xo < xendo; xo++, xi++) {
            const uint8_t* const inb = pp + yi * pw + xi;
            uint8_t* const outb = surface->data + (yo * surface->width + xo) * 4;
            const float inf[4] = { pr, pg, pb, (pa * ((float)*inb/0xffu)) };
            float outf[4] = { (float)outb[R]/0xffu, (float)outb[G]/0xffu, (float)outb[B]/0xffu, (float)outb[A]/0xffu };
            float outa = inf[A] + outf[A] * (1.0f - inf[A]);
            if (outa > 0.0f) {
                outf[R] = (inf[R] * inf[A] + outf[R] * outf[A] * (1.0f - inf[A])) / outa;
                outf[G] = (inf[G] * inf[A] + outf[G] * outf[A] * (1.0f - inf[A])) / outa;
                outf[B] = (inf[B] * inf[A] + outf[B] * outf[A] * (1.0f - inf[A])) / outa;
            }
            outf[A] = outa;

            outb[R] = 0xffu * outf[R];
            outb[G] = 0xffu * outf[G];
            outb[B] = 0xffu * outf[B];
            outb[A] = 0xffu * outf[A];
        }
    }
}

T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, T_TrueTypeFont* font, const char* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
{
    const T_SurfaceData* const surface = qstb_get_surfacedata(surfaceHandle);
    if (surface == NULL) {
        return (T_SizeInt) {0, 0}; //TODO what to do
    }
    {
        uint32_t* const surfaceData = (uint32_t*)surface->data;
        if (surfaceData == NULL) {
            return (T_SizeInt) {0, 0}; //TODO what to do
        }
    }

    const size_t len = strlen(text);
    if (len == 0) {
        return (T_SizeInt) {0, 0}; //TODO what to do
    }

    struct T_TrueTypeFont2 f;
    qstb_InitFont(font, &f);

    float cx = 0;
    {
        assert(len != 0);
        int32_t x0 = 0;
        stbtt_GetCodepointBitmapBoxSubpixel(&f.font, text[0], f.scale, f.scale, 0.0f, 0.0f,
            &x0, NULL, NULL, NULL);
        // assert(x0 <= 0); //TODO else what to do
        cx += -x0;
    }
    int32_t cy = f.scale * f.ascent; //TODO ceil? TODO linebreaks, centering
    struct {
        int32_t x0, y0, x1, y1, advanceWidth;
    } box = {};
    for (int32_t iText = 0; iText < len; iText++) {
        const char32_t c = text[iText]; //TODO mbrtoc32
        memset(&box, 0x00, sizeof(box));
        stbtt_GetCodepointBitmapBoxSubpixel(&f.font, c, f.scale, f.scale, (cx - floorf(cx)), 0.0f
            , &box.x0, &box.y0, &box.x1, &box.y1);
        stbtt_GetCodepointHMetrics(&f.font, c, &box.advanceWidth, NULL);
        struct {
            uint8_t* p;
            int32_t w, h, xoff, yoff;
        } bmp = {};
        bmp.p = stbtt_GetCodepointBitmapSubpixel(&f.font, f.scale, f.scale, (cx - floorf(cx)), 0.0f, c
            , &bmp.w, &bmp.h, &bmp.xoff, &bmp.yoff); //TODO MakeCodepointBitmapSubpixel, MakeGlyphBitmapSubpixel
        if (bmp.p != NULL) {
            qstb_MemBlend(surface, cx + box.x0, cy + box.y0, bmp.p, bmp.w, bmp.h, r, g, b, a);
            free(bmp.p);
        }
        if (iText < len - 1) {
            cx += f.scale * box.advanceWidth;
            cx += f.scale * stbtt_GetCodepointKernAdvance(&f.font, c, text[iText + 1]);
        }
    }

    return (T_SizeInt) {floorf(cx) + box.x1, f.scale * (f.ascent - f.descent)};
    // return (T_SizeInt) { 0, 0 };
}