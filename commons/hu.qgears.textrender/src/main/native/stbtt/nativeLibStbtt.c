#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <uchar.h>

#define STB_TRUETYPE_IMPLEMENTATION
#define STBTT_STATIC
#include "stb_truetype.h"

#include "nativeLibStbtt.h"

#include "qstb_utf8.h"
#include "qstb_line.h"

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

//--------------------------------------------------

static void qstb_InitFont(T_TrueTypeFont* font) {
    //TODO cache

    assert(!font->stb.inited);
    if (font->stb.inited) {
        return;
    }
    assert(font->stb.font.data == NULL);

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
        ret = stbtt_InitFont(&font->stb.font, bufFont, 0);
        assert(ret != 0); // no error
        assert(font->stb.font.data == bufFont); // can free later
    }
    stbtt_GetFontVMetrics(&font->stb.font, &font->stb.ascent, &font->stb.descent, &font->stb.lineGap);
    assert(0 <= font->stb.ascent);
    assert(font->stb.descent <= 0);
    stbtt_GetFontBoundingBox(&font->stb.font, &font->stb.x0, &font->stb.y0, &font->stb.x1, &font->stb.y1);
    assert(font->stb.x0 <= font->stb.x1);
    assert(font->stb.y0 <= font->stb.y1);
    // assert(result->ascent <= -result->y0);
    font->stb.scale = stbtt_ScaleForPixelHeight(&font->stb.font, font->fontSize);
    assert(0.0f <= font->stb.scale);

    font->stb.inited = true;
}

T_SizeInt qstb_layoutTextPrivate(T_TrueTypeFont* font, const char* text,
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{

    const bool wasInited = font->stb.inited;
    qstb_InitFont(font);

    int32_t nLines = 0;
    double maxLineWidth = 0;
    struct utf8 utf8 = utf8_init(text);
    struct LineInfo line = line_peek((struct LineInfo){}, utf8, width
        , font->stb.scale, font->letterSpacing, &font->stb.font, wrapMode);
    do { //even the empty string is at least one line long
        nLines += 1;
        double lineWidth = line_width(line, font->stb.scale, font->letterSpacing);
        if (maxLineWidth < lineWidth) {
            maxLineWidth = lineWidth;
        }
        line = line_next(line, utf8, width
            , font->stb.scale, font->letterSpacing, &font->stb.font, wrapMode);
    } while (line.end < utf8.len);

    //TODO special handling for 0 ink width lines?

    if (!wasInited) {
        font->stb.inited = false;
        free(font->stb.font.data);
    }

    return (T_SizeInt) {
        .width = ceil(maxLineWidth),
        .height = ceil(nLines * (font->stb.ascent - font->stb.descent + font->stb.lineGap) * font->stb.scale)
    };
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
    assert(0.0f <= pr && pr <= 1.0f);
    assert(0.0f <= pg && pg <= 1.0f);
    assert(0.0f <= pb && pb <= 1.0f);
    assert(0.0f <= pa && pa <= 1.0f);

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
            if (*inb == 0x00u) {
                continue;
            }
            uint8_t* const outb = surface->data + (yo * surface->width + xo) * 4;
            const float inf[4] = { pr, pg, pb, (pa * ((float)*inb/0xffu)) };
            float outf[4] = { (float)outb[R]/0xffu, (float)outb[G]/0xffu, (float)outb[B]/0xffu, (float)outb[A]/0xffu };
            const float outa = inf[A] + outf[A] * (1.0f - inf[A]);
            if (outa > 0.0f) {
                outf[R] = (inf[R] * inf[A] + outf[R] * outf[A] * (1.0f - inf[A])) / outa;
                outf[G] = (inf[G] * inf[A] + outf[G] * outf[A] * (1.0f - inf[A])) / outa;
                outf[B] = (inf[B] * inf[A] + outf[B] * outf[A] * (1.0f - inf[A])) / outa;
            }
            outf[A] = outa;

            assert(0.0f <= outf[R] && outf[R] <= 1.0f);
            assert(0.0f <= outf[G] && outf[G] <= 1.0f);
            assert(0.0f <= outf[B] && outf[B] <= 1.0f);
            assert(0.0f <= outf[A] && outf[A] <= 1.0f);
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
    /*
     * TODO count lines, implement valign bottom, valign middle
     *      Should valign bottom/middle default to valign top if the text is too tall?
     *      What about leading/trailing empty lines?
     * TODO check each line's width. Implement halign right, middle, justify
     * TODO qstb_MemBlend clips on surface.width and surface.height. Let params width and height be provided instead, and implement CLIP
     */

    const T_SurfaceData* const surface = qstb_get_surfacedata(surfaceHandle);
    if (surface == NULL || surface->data == NULL) {
        return (T_SizeInt) {0, 0};
    }

    const bool wasInited = font->stb.inited;
    qstb_InitFont(font);

    struct {
        uint8_t* p;
        int32_t w, h;
    } tmp = {};
    {
        int32_t unscaled_height = font->stb.y1 - font->stb.y0 + 1;
        int32_t unscaled_width = font->stb.x1 - font->stb.x0 + 1;
        tmp.w = ceil(unscaled_width * font->stb.scale) + 1;
        tmp.h = ceil(unscaled_height * font->stb.scale) + 1;
        tmp.p = calloc(tmp.w * tmp.h, 1);
    }

    struct utf8 s = utf8_init(text);
    struct LineInfo line = line_peek((struct LineInfo){}, s, width
        , font->stb.scale, font->letterSpacing, &font->stb.font, wrapMode);

    struct {
        //unscaled, counted from param x & y, y increases down
        int32_t x, y;
    } idraw = {
        .y = font->stb.ascent
    };

    do { // there is always a first/last line (even in the empty string). Stop once the processed line has just been the last line.
        s = utf8_seek(s, line.off);
        while (is_ignore(s.codepoint) && s.end <= line.end) {
            s = utf8_read(s);
        }

        idraw.x = 0;
        if (s.end <= line.end && s.codepoint != '\0') {
            int32_t leftSideBearing = 0;
            stbtt_GetCodepointHMetrics(&font->stb.font, s.codepoint, NULL, &leftSideBearing);
            idraw.x = leftSideBearing;
        }

        int32_t iPrintable = 0;
        while (s.end <= line.end && s.codepoint != '\0') {
            assert(is_print(s.codepoint));

            int32_t advanceWidth = 0;
            stbtt_GetCodepointHMetrics(&font->stb.font, s.codepoint, &advanceWidth, NULL);

            if (is_graph(s.codepoint)) {
#define SHIFT(x) ((x) - floor((x)))
                float shift_x;
                const float shift_y = SHIFT(idraw.y * font->stb.scale);

                struct {
                    int32_t lsb;
                    int32_t y0;
                } codepoint = {};
                {
                    assert(is_graph(s.codepoint));
                    stbtt_GetCodepointHMetrics(&font->stb.font, s.codepoint, NULL, &codepoint.lsb);
                    shift_x = SHIFT((idraw.x + codepoint.lsb) * font->stb.scale + iPrintable * font->letterSpacing);
#undef SHIFT
                    stbtt_GetCodepointBitmapBoxSubpixel(&font->stb.font, s.codepoint
                        , font->stb.scale, font->stb.scale, shift_x, shift_y
                        , NULL, &codepoint.y0, NULL, NULL);
                }

                struct {
                    //scaled, counted from param x & y, y increases down
                    int32_t x, y;
                } icorner = {
                    //shift_x is calculated without lsb...
                    .x = (idraw.x + codepoint.lsb) * font->stb.scale + iPrintable * font->letterSpacing,
                    .y = (idraw.y * font->stb.scale) + codepoint.y0
                };

                memset(tmp.p, 0x00, tmp.w * tmp.h);
                stbtt_MakeCodepointBitmapSubpixel(&font->stb.font, tmp.p, tmp.w, tmp.h, tmp.w
                    , font->stb.scale, font->stb.scale, shift_x, shift_y
                    , s.codepoint);

                qstb_MemBlend(surface, x + icorner.x, y + icorner.y
                    , tmp.p, tmp.w, tmp.h
                    , r, g, b, a);
            }

            //loop variables...
            iPrintable += 1;
            idraw.x += advanceWidth;
            s = utf8_read(s);
            while (is_ignore(s.codepoint) && s.end <= line.end) {
                s = utf8_read(s);
            }
        }

        line = line_next(line, s, width, font->stb.scale, font->letterSpacing, &font->stb.font, wrapMode);
        idraw.y += font->stb.ascent - font->stb.descent + font->stb.lineGap;
    } while (line.end < s.len);

    T_SizeInt ret = qstb_layoutTextPrivate(font, text, hAlign, vAlign, width, height, wrapMode);
    if (wasInited) {
        font->stb.inited = false;
        free(font->stb.font.data);
    }
    free(tmp.p);
    return ret;
}
