#include "nativeLibLibschrift.h"
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "util.h"

/**
 * Iterating over codepoints:
 * 
 * uint32_t codepoint;
 * const uint16_t* reader = text;
 * while(foobar(codepoint = utf16Peek(reader))) {
 *      advanceBeforeRender(... codepoint, &penx);
 *      renderGlyph(... codepoint, penx);
 *      advanceAfterRender(... codepoint, &penx);
 *      utf16Skip(&reader);
 * }
 * 
 * Iterating over lines:
 * 
 * T_Result_PrescanLine lineData = {0};
 * const uint16_t* reader = text;
 * do {
 *      prescanLine(... reader, &lineData);
 *      ...
 *      reader = lineData.nextLineStart;
 * } while (!lineData.wasLastLine);
 */

// Structure to represent surface data
typedef struct {
    uint8_t* data;
    int32_t width;
    int32_t height;
    /**
     * true : BGRA premultiplied.
     * false : ALPHA
     */
    bool color;
} T_SurfaceData;

typedef struct {
    int32_t x;
    int32_t y;
} T_IPoint;
typedef struct {
    SCHRIFT_F64 x;
    SCHRIFT_F64 y;
} T_DPoint;

typedef struct {
    SFT sft;
    T_TrueTypeFont* font;
    T_FontSettings* fontSettings;
    /*The line metrics of currently applied font, loaded by libschrift - lazy init*/
	SFT_LMetrics lineMetrics;
    T_IPoint minPen;
    T_IPoint maxPen;
    uint32_t color;
    uint32_t wrapMode;
    uint32_t hAlign;
    uint32_t vAlign;
} T_RenderData;

typedef struct {
    uint32_t nVariableWidthSpaces;
    const uint16_t* lineStart;
    //exclusive, consistent with `len`
    const uint16_t* lineEnd;
    //inclusive
    const uint16_t* nextLineStart;
    bool wasLastLine;
    SCHRIFT_F64 width;
    SFT_LMetrics lMetrics;
} T_Result_PrescanLine;

typedef enum {
    QLS_WRAP_CHAR,
    QLS_WRAP_WORD,
    QLS_WRAP_WORDCHAR,
    QLS_WRAP_NONE
} E_QLS_WRAP;

typedef struct {
	const uint16_t* start;
	const uint16_t* end;
	const uint16_t* pos;
} UTF16_Reader;

enum { HALIGN_LEFT, HALIGN_CENTER, HALIGN_RIGHT, HALIGN_JUSTIFY }; //TODO single source of truth
enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM };

#define CP_COMBINING_LOW_LINE 0x332u
#define CP_ZERO_WIDTH_SPACE 0x200Bu

#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define LIMIT(x,min,max) ((x) < (min) ? (min) : ((x) > (max) ? (max) : (x)))
/* Glyph bitmaps up to this many bytes are rendered into a stack buffer, larger ones are malloc'd.
 * 4096 = 64x64 px; kept small because sft_render itself uses a large stack buffer. */
#define GLYPH_STACK_BUFFER_SIZE 4096
/* Allocate values on the stack if they are small enough, else spill to heap (same as STACK_ALLOC in schrift.c,
 * but memory is NOT zeroed). `var` must be a declared pointer variable (not a struct member). */
#define QLS_STACK_ALLOC(var, type, thresh, count) \
	type var##_stack_[thresh]; \
	var = ((size_t)(count) <= (size_t)(thresh)) ? var##_stack_ : (type*)malloc(sizeof(type) * (size_t)(count));
/* Frees `var` only if it was spilled to the heap by QLS_STACK_ALLOC. */
#define QLS_STACK_FREE(var) \
	do { if ((var) != var##_stack_) free(var); } while (0)
#define PIX(r)  SCHRIFT_F32_trunc_to_u8_to_u32((SCHRIFT_F32_mul_u32(r , 0xFFu)))



/* Line height of a SFT_LMetrics* : ascender - descender + lineGap */
#define LINE_HEIGHT(lm) SCHRIFT_F64_add(SCHRIFT_F64_sub(lm.ascender, lm.descender), lm.lineGap)

static inline void preprocess_parameter_color(SCHRIFT_F32* a);
static inline void preprocess_parameter_fontSettings(T_FontSettings* fontSettings);
static inline uint32_t blend_bgra_premultiplied(uint32_t dst, uint32_t pcolor, uint8_t mask);
/* Check whether the x0, x1 range is within the rangeMinIncluding and rangeMaxNotIncluding range. */
static inline bool is_valid_range(int32_t rangeMinIncluding, int32_t rangeMaxIncluding, int32_t x0, int32_t x1);
/** Copy pixels from source image img to the target surface "surface".
 * @param startx x coordinate on the target to start drawing pixels of img from
 * @param starty y coordinate on the target to start drawing pixels of img from
 * @param surface target to copy pixels onto - color or alpha surface
 * @param img source to copy pixels from - grayscale image of the character in libschrift format
 * @param color in case of color surface output this color is used to draw the character.
 */
static inline void copy_rect(int32_t startx, int32_t starty, T_SurfaceData* surface, SFT_Image* img, uint32_t color);
/**
 * Renders only if (surface != NULL and) r->fontSettings->isUnderline == true
 */
static inline void render_underline_opt(T_ErrorHandler* eh, const T_RenderData* r, T_SurfaceData* surface, SCHRIFT_F64 peny, SCHRIFT_F64 penstartx, SCHRIFT_F64 penendx);
static inline void render_underline(T_ErrorHandler* eh, const T_RenderData* r, T_SurfaceData* surface, SCHRIFT_F64 peny, SCHRIFT_F64 penstartx, SCHRIFT_F64 penendx);
static inline void qls_apply_font(T_ErrorHandler *eh, T_RenderData* r,  uint32_t fontIndex);
static T_SurfaceData* qls_get_surfacedata(uint64_t id);
/**
 * Looks up the glyph belonging to the given codepoint. If the codepoint is not
 * present in the currently loaded font, the configured fallback fonts are
 * queried, too.
 *
 * @return the glyph id, or 0 if the codepoint was not found in any of the
 *         fonts. In case of error the error handler is filled and 0 returned.
 */
static inline SFT_Glyph qls_lookup(T_ErrorHandler* eh, T_RenderData* rData, uint32_t codepoint);
static inline void advancePen(
		T_ErrorHandler* eh, T_RenderData * rData, uint32_t prevCodepoint, uint32_t currentCodepoint, T_DPoint* pen,T_SurfaceData* surface, SCHRIFT_F64 spaceJustification);

/** Returns the current code point pointed by reader->pos. Advances the pos towards end. Guarantees that iteration ends when whole input was read.
 * Stops advancing and returns 0 in case the end was reached.
 */
static inline uint32_t utf16_read_codepoint(UTF16_Reader* reader );
/**
 * Same as utf16_read_codepoint, but leaves the reader position unchanged.
 */
static inline uint32_t utf16_peek_codepoint(UTF16_Reader* reader );
static inline bool ctypeIsLineEnding(uint32_t c);
static inline bool ctypeIsSpace(uint32_t c);
static inline bool ctypeIsGraphical(uint32_t c);
static inline bool ctypeIsGraphemeBase(uint32_t c);
static inline int32_t wholePart(SCHRIFT_F64 d);
static inline SCHRIFT_F64 fractionalPart(SCHRIFT_F64 d);
static inline int32_t renderData_GetWidth(T_RenderData* r);
/**
 * Use this function to
 *  * Get the number of codepoints to render in one line
 *  * Get the start of the next line
 * 
 * Includes logic to
 *  * Include leading whitespace
 *  * Let the next line skip this line's trailing whitespace and hard wrap
 *  * Check line width with some particular semantics (i.e. not taking left-side bearing and right-side bearing into account)
 *  * Check for word break opportunities (Simple space, non-space check)
 * 
 * TODO: Improve line breaking algorithm to "minimize ruggedness"? (e.g. to protect against two words falling on the last line and being splayed out by justification)
 * TODO: Support variable line-height (e.g. for ideographs coming from a fallback font with different line metrics)
 * TODO: Let lines be rendered either in one or piecemeal, while remaining pixel perfect?
 * TODO: Support more Unicode, e.g. ideographic space (how should it interact with justification etc.)
 * TODO: Introduce grapheme layer, or decide on partial support and canonize input (e.g. the same accented letter can be represented as either one or two codepoints!)
 */
static inline void prescanLine(T_ErrorHandler* eh, T_RenderData* rData, UTF16_Reader* reader, T_Result_PrescanLine* outResult);
static inline int32_t prescanText(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text,uint32_t textLen);
static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text,uint32_t textLen, T_SurfaceData* surface);
static inline int32_t getSpareWidth(int32_t boxWidth, SCHRIFT_F64 lineWidth);


/*********************************************/
/*** External function implementations     ***/
/*********************************************/

void qls_clearSurfacePrivate(uint64_t id) {
    T_SurfaceData* surface = qls_get_surfacedata(id);
    if (surface && surface->data) {
		uint32_t pixelSize = surface->color ? 4u : 1u;
		int32_t stride = surface->color ?  surface->width : ((surface->width + 3) & ~3);
        uint32_t sz = (uint32_t)surface->height * (uint32_t)stride * pixelSize;
        memset(surface->data, 0, sz);
    }
}

uint64_t qls_createSurfaceWithDataPrivate(T_ErrorHandler *eh, uint8_t* data, int32_t w, int32_t h, int32_t pixelFormat)
{
    // Allocate memory for surface data structure
    T_SurfaceData* surfaceData = (T_SurfaceData*)malloc(sizeof(T_SurfaceData));
    if (surfaceData == NULL) {
        return 0; // Return 0 on allocation failure
    }
    
    switch (pixelFormat)
    {
    case ENICO_BGRA:
        surfaceData->color = true;
        break;
    case ENICO_ALPHA:
        surfaceData->color = false;
        break;
    default:
        ERROR(eh,QLS_ERROR_UNSUPPORTED_PIXEL_FORMAT, "Unsupported pixel format %d",pixelFormat);
        break;
    }

    // Initialize the surface data
    surfaceData->data = data;
    surfaceData->width = w;
    surfaceData->height = h;
    
    // Return the memory address as the handle
    return (uint64_t)(uintptr_t)surfaceData;
}

void qls_disposeSurfacePrivate(uint64_t surfaceHandle)
{
    // Check if handle is valid
    if (surfaceHandle == 0) {
        return;
    }
    
    // Cast handle back to T_SurfaceData pointer and free it
    T_SurfaceData* surfaceData = (T_SurfaceData*)(uintptr_t)surfaceHandle;
    free(surfaceData);
}

T_SizeInt qls_renderTextPrivate(T_ErrorHandler* errorHandler, uint64_t surfaceHandle, T_FontSettings* fontSettings, const uint16_t* text, uint32_t textLen,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            SCHRIFT_F32 r, SCHRIFT_F32 g, SCHRIFT_F32 b, SCHRIFT_F32 a, bool clip, uint32_t wrapMode)
{
    preprocess_parameter_color(&r);
    preprocess_parameter_color(&g);
    preprocess_parameter_color(&b);
    preprocess_parameter_color(&a);
    preprocess_parameter_fontSettings(fontSettings);

    T_SizeInt result = {0, 0};
    T_TrueTypeFont* font = fontSettings->fonts[0];
    T_SurfaceData* surface = qls_get_surfacedata(surfaceHandle);
    if (surface) {
        uint32_t* surfaceData = (uint32_t*)surface->data;
        if (surfaceData) 
        {
            T_RenderData rData = {0};
            rData.fontSettings = fontSettings;
            rData.minPen.x = x;
            rData.minPen.y = y;
            rData.maxPen.x = x + width;
            rData.maxPen.y = y + height;
            rData.wrapMode = wrapMode;
            rData.hAlign = hAlign;
            rData.vAlign = vAlign;
            if (rData.maxPen.y > rData.minPen.y && rData.maxPen.x > rData.minPen.x)
            {
                qls_apply_font(errorHandler, &rData,0);
                if (errorHandler->code == QLS_ERROR_OK)
                {
                    rData.color = PIX(r) | (PIX(g) << 8) | (PIX(b) << 16) | (PIX(a) << 24);
                    result = layoutAndRender(errorHandler, &rData, text, textLen, surface);
                    // if (errorHandler->code == QLS_ERROR_OK) {
                    // } else {
                    // }
                }
            }
            else
            {
                //specified target rectangle is invalid or empty
            }
        } 
        else
        {
            ERROR(errorHandler,QLS_ERROR_SURFACE_DATA_NULL,"Surface data null");
        }
    } else
    {
        ERROR(errorHandler,QLS_ERROR_INVALID_SURFACE,"Invalid surface id");
    }
    
    return result;
}

T_SizeInt qls_layoutTextPrivate(T_ErrorHandler* errorHandler, T_FontSettings* fontSettings, const uint16_t* text,
                            uint32_t textLen, uint32_t hAlign, int32_t width, uint32_t wrapMode)
{
    preprocess_parameter_fontSettings(fontSettings);

    T_SizeInt result = {0,0};
    T_RenderData r = {0};
    r.fontSettings = fontSettings;
    r.maxPen.x = width;
    r.maxPen.y = INT32_MAX;
    r.sft.flags = SFT_DOWNWARD_Y;
    r.wrapMode = wrapMode;
    r.hAlign = hAlign;
    r.vAlign = VALIGN_TOP;
    qls_apply_font(errorHandler,&r,0);
    if (errorHandler->code == QLS_ERROR_OK)
    {
        result = layoutAndRender(errorHandler, &r, text,textLen, NULL);
        // if (errorHandler->code == QLS_ERROR_OK)
        // {
        // }
    }
    return result;
}

/* Build switch: define MAP_FONTS_WITH_MMAP to mmap the TTF file (sft_loadfile).
 * By default the whole file is read into a malloc'd buffer (T_TrueTypeFont.fontData, freed by qls_free_font). */
#ifdef MAP_FONTS_WITH_MMAP
void qls_load_font(T_ErrorHandler *eh, T_TrueTypeFont *font) {
	if (font->font == NULL) {
		font->font = sft_loadfile(font->ttfFilePath);
		if (font->font == NULL) {
			ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF load failed %s",
					filename(font->ttfFilePath));
			return;
		}
	}
}
#else
void qls_load_font(T_ErrorHandler *eh, T_TrueTypeFont *font) {
	if (font->font != NULL) {
		return;
	}
	FILE* f = fopen(font->ttfFilePath, "rb");
	if (f == NULL) {
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF open failed %s", filename(font->ttfFilePath));
		return;
	}
	long fileSize = -1;
	if (fseek(f, 0, SEEK_END) == 0) {
		fileSize = ftell(f);
	}
	if (fileSize <= 0) {
		fclose(f);
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF size query failed (%ld) %s", fileSize, filename(font->ttfFilePath));
		return;
	}
	if (fseek(f, 0, SEEK_SET) != 0) {
		fclose(f);
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF rewind failed %s", filename(font->ttfFilePath));
		return;
	}
	size_t size = (size_t)fileSize;
	void* data = malloc(size);
	if (data == NULL) {
		fclose(f);
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF out of memory (%lu bytes) %s",
				(unsigned long)size, filename(font->ttfFilePath));
		return;
	}
	size_t read = fread(data, 1, size, f);
	fclose(f);
	if (read != size) {
		free(data);
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF read failed %s", filename(font->ttfFilePath));
		return;
	}
	font->font = sft_loadmem(data, size);
	if (font->font == NULL) {
		free(data);
		ERROR(eh, QLS_ERROR_FONT_LOAD, "TTF load failed %s", filename(font->ttfFilePath));
		return;
	}
	font->fontData = data;
}
#endif

void qls_free_font(T_TrueTypeFont* font) {
	if (font->font != NULL) {
		sft_freefont(font->font);
		font->font = NULL;
	}
	if (font->fontData != NULL) {
		free(font->fontData);
		font->fontData = NULL;
	}
}

/*********************************************/
/***   Static function implementations     ***/
/*********************************************/
static inline void preprocess_parameter_color(SCHRIFT_F32* a) {
    //TODO add bespoke handling, for NaN?
    if (SCHRIFT_F64_compare_lt(SCHRIFT_F64_from_SCHRIFT_F32(*a), SCHRIFT_F64_const_zero)) {
        *a = SCHRIFT_F32_from_float(0.0f);
    } else if (SCHRIFT_F64_compare_lt(SCHRIFT_F64_const_1, SCHRIFT_F64_from_SCHRIFT_F32(*a))) {
        *a = SCHRIFT_F32_from_float(1.0f);
    } else {
        // *a = SCHRIFT_F32_floor_to_Zper0x1p8(*a);
    }
}
static inline void preprocess_parameter_fontSettings(T_FontSettings* fontSettings) {
    if (fontSettings != NULL) {
        //We do not expect fractional font sizes, in reality -> no difference
        //We can re-add excess precision if want to: it will not cause excess precision in pen.x, pen.y
        SCHRIFT_F32_ceil_to_Zper0x1p8(fontSettings->fontSize);
        //We do not expect negative letterSpacing
        SCHRIFT_F64_floor_to_Zper0x1p8(fontSettings->letterSpacing);
    }
}

static inline uint32_t blend_bgra_premultiplied(uint32_t dst, uint32_t pcolor, uint8_t alpha)
{
    // pcolor is packed in RGBA byte order (byte0=R, byte1=G, byte2=B, byte3=A)
    uint32_t pR =  pcolor        & 0xFF;
    uint32_t pG = (pcolor >>  8) & 0xFF;
    uint32_t pB = (pcolor >> 16) & 0xFF;
    uint32_t pA = ((pcolor >> 24) & 0xFF);

    // dst is stored in BGRA byte order (byte0=B, byte1=G, byte2=R, byte3=A)
    uint32_t db = dst        & 0xFF;
    uint32_t dg = (dst >>  8) & 0xFF;
    uint32_t dr = (dst >> 16) & 0xFF;
    uint32_t da = (dst >> 24) & 0xFF;
    
    // combine the color's own alpha with the glyph coverage mask
    uint32_t srcAlpha = pA * alpha / 255;
    uint32_t one_minus_alpha = 255 - srcAlpha;

    uint32_t chan_b = (pB * srcAlpha + db * one_minus_alpha) / 255;
    uint32_t chan_g = (pG * srcAlpha + dg * one_minus_alpha) / 255;
    uint32_t chan_r = (pR * srcAlpha + dr * one_minus_alpha) / 255;
    uint32_t chan_a = srcAlpha + da * one_minus_alpha / 255;

    // result is written back in BGRA order, premultiplied by alpha
    return (chan_a << 24) | (chan_r << 16) | (chan_g << 8) | chan_b;
}
static inline bool is_valid_range(int32_t rangeMinIncluding, int32_t rangeMaxIncluding, int32_t x0, int32_t x1)
{
    if(x0<rangeMinIncluding){ return false;}
    if(x0>rangeMaxIncluding){ return false;}
    if(x1<x0){ return false;}
    if(x1>rangeMaxIncluding){ return false;}
    return true;
}
static inline void copy_rect(int32_t startx, int32_t starty, T_SurfaceData* surface, SFT_Image* img, uint32_t color)
{
    // Ensure the source image and destination surface are valid
    if (!surface || !img || !img->pixels) {
        return;
    }

    int32_t min_src_x = 0;
    if (startx < 0) {
        min_src_x = -startx;
        if (min_src_x >= img->width){
            return;
        }
    }
    int32_t max_src_x = 0;
    int32_t distance_from_right =  surface->width - (img->width + startx);
    if (distance_from_right > 0) {
        max_src_x = img->width;
    } else {
        max_src_x = img->width + distance_from_right;
    }

    if (max_src_x <= min_src_x){
        return;
    }

    int32_t min_src_y = 0;
    if (starty < 0) {
        min_src_y = -starty;
        if (min_src_y >= img->height){
            return;
        }
    }
    int32_t max_src_y = 0;
    int32_t distance_from_down =  surface->height - (img->height + starty);
    if (distance_from_down > 0) {
        max_src_y = img->height;
    } else {
        max_src_y = img->height + distance_from_down;
    }

    if (max_src_y <= min_src_y){
        return;
    }

    int32_t i = 0;
    int32_t j = 0;
    int32_t dsti = starty;
    int32_t dstj = startx;
    uint32_t* surfaceData4 = (uint32_t*)surface->data;
    uint8_t* surfaceData = surface->data;
    // non-color surfaces need each row padded to a 4-byte boundary; color rows (4 bytes/pixel) are aligned trivially
    int32_t rowStride = surface->color ? surface->width : ((surface->width + 3) & ~3);
    
    uint8_t* imageData = ((uint8_t*)img->pixels);
    
    // Validate loop end points to avoid endless loop or bad addressing. In case of error there is no means to signal error.
    // Error is only possible if there is an internal error in the arithmetics above.
    if (!is_valid_range(0, img->height, min_src_y, max_src_y))
    {
        return;
    }
    if (!is_valid_range(0, img->width, min_src_x, max_src_x))
    {
        return;
    }
    dsti = starty+min_src_y;
    int32_t dstiend = dsti + max_src_y - min_src_y - 1;
    if (!is_valid_range(0, surface->height, dsti, dstiend))
    {
        return;
    }
    dstj = startx+min_src_x;
    int32_t dstjend = dstj + max_src_x - min_src_x -1;
    if (!is_valid_range(0, surface->width, dstj, dstjend))
    {
        return;
    }
    // row-major traversal (rows outer, columns inner) for cache-friendly access of both buffers
    for (i = min_src_y; i < max_src_y; i++, dsti++ ) {
        for (j = min_src_x,dstj = startx+min_src_x; j < max_src_x; j++, dstj++ ) {
            int32_t dst_pix = (dsti*rowStride) + dstj;
            int32_t src_pix = (i*img->width) + j;
            uint8_t src_alpha = imageData[src_pix];
            if (src_alpha > 0) {  // Only copy non-transparent pixels
                if (surface->color) {
                    surfaceData4[dst_pix] = blend_bgra_premultiplied(surfaceData4[dst_pix], color,src_alpha);
                } else {
                    uint32_t da = surfaceData[dst_pix];
                    surfaceData[dst_pix] = (uint8_t)(src_alpha + da * (255u - src_alpha) / 255u);
                }
            }
        }
    }
}

static inline void render_underline_opt(T_ErrorHandler* eh, const T_RenderData* r, T_SurfaceData* surface, SCHRIFT_F64 peny, SCHRIFT_F64 penstartx, SCHRIFT_F64 penendx) {
    if (r != NULL && r->fontSettings != NULL && r->fontSettings->isUnderline) {
        render_underline(eh, r, surface, peny, penstartx, penendx);
    }
}

static inline void render_underline(T_ErrorHandler* eh, const T_RenderData* r, T_SurfaceData* surface, SCHRIFT_F64 peny, SCHRIFT_F64 penstartx, SCHRIFT_F64 penendx) {
    if (r != NULL /*&& ignoring r->fontSettings->isUnderline*/ && surface != NULL && surface->data != NULL) {
        const int32_t y = SCHRIFT_F64_ceil_to_int(peny); //TODO consider nextafterf before ceil_to_int
        if (0 <= y && y < surface->height) {

            const int32_t x0 = MAX(0, SCHRIFT_F64_floor_to_int(penstartx)); // floor to int: pen positions in [0,1) point to pixel[0]
            const int32_t xmax = MIN(SCHRIFT_F64_floor_to_int(penendx), surface->width); // exclusive

            //XXX keep code in synch with copy_rectangle(...)
            uint32_t* surfaceData4 = (uint32_t *)surface->data;
            uint8_t* surfaceData = surface->data;
            const int32_t rowStride = surface->color ? surface->width : ((surface->width + 3) & ~3);
            
            if (!is_valid_range(0, surface->width, x0, xmax))
            {
                // Avoid indexing error in case of internal error bad arithmetics.
                return;
            }
            
            for (int32_t x = x0; x < xmax; ++x) {
                const int32_t dst_pix = y * rowStride + x;
                if (surface->color)
                {
                    surfaceData4[dst_pix] = blend_bgra_premultiplied(surfaceData4[dst_pix], r->color, 255u);
                }
                else
                {
                    surfaceData[dst_pix] = (uint8_t)(255u);
                }
            }
        }
    }
}

static inline void qls_apply_font(T_ErrorHandler *eh, T_RenderData* r, uint32_t fontIndex)
{
    SFT* sft = &(r->sft);
	T_FontSettings *fontSettings = r->fontSettings;
    //xScale, fontSize: precision OK
    sft->xScale = SCHRIFT_F64_from_SCHRIFT_F32(fontSettings->fontSize);
    sft->yScale = SCHRIFT_F64_from_SCHRIFT_F32(fontSettings->fontSize);
    sft->flags = SFT_DOWNWARD_Y;

	if (fontIndex < fontSettings->fontsCount)
	{
		r->font = fontSettings->fonts[fontIndex];
		sft->font = r->font->font;
	}
	else
	{
		ERROR(eh, QLS_ERROR_FONT_APPLY, "Invalid fontIndex %u ", fontIndex);
		return;
	}

	if (sft_lmetrics(sft, &(r->lineMetrics)) < 0) {
		ERROR(eh, QLS_ERROR_LINE_METRICS,
				"Failed to init line metrics of font %s",
				filename(r->font->ttfFilePath));
		return;
	} else {
        r->lineMetrics.ascender = SCHRIFT_F64_ceil_to_Zper0x1p8(r->lineMetrics.ascender);
        r->lineMetrics.descender = SCHRIFT_F64_floor_to_Zper0x1p8(r->lineMetrics.descender);
        r->lineMetrics.lineGap = SCHRIFT_F64_ceil_to_Zper0x1p8(r->lineMetrics.lineGap);
		LOG("LineMetrics of %s asc %g, desc %g, gap %g",
				filename(r->font->ttfFilePath),
				SCHRIFT_F64_to_double(r->lineMetrics.ascender),
				SCHRIFT_F64_to_double(r->lineMetrics.descender),
				SCHRIFT_F64_to_double(r->lineMetrics.lineGap));
	}
}

static T_SurfaceData* qls_get_surfacedata(uint64_t id) {
    // Cast the handle back to T_SurfaceData pointer
    return (T_SurfaceData*)(uintptr_t)id;
}

static inline SFT_Glyph qls_lookup(T_ErrorHandler* eh, T_RenderData* rData, uint32_t codepoint)
{
	SFT_Glyph glyph = 0;
	SFT* sft = &rData->sft;
	T_FontSettings* fontSettings = rData->fontSettings;
	// Always search in fontSettings->fonts order, starting from the primary font
	for (uint32_t i = 0; i < fontSettings->fontsCount; i++) {
		T_TrueTypeFont* font = fontSettings->fonts[i];
		if (font != rData->font)
		{
			qls_apply_font(eh,rData, i);
			if (eh->code != QLS_ERROR_OK){
				return 0;
			}
		}
		if (sft_lookup(sft, codepoint, &glyph) < 0) {
			ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", codepoint);
			return 0;
		}
		if (glyph != 0) {
			return glyph;
		}
	}
	// Not found in any font: the missing glyph of the primary font is rendered
	if (fontSettings->fontsCount > 0 && rData->font != fontSettings->fonts[0]) {
		qls_apply_font(eh,rData, 0);
		if (eh->code != QLS_ERROR_OK){
			return 0;
		}
	}
	LOG("WARN codepoint not found %d", codepoint);
	return 0;
}

static inline bool ctypeIsLineEnding(uint32_t c) {
    switch (c) {
        case '\0': case '\r': case '\n':
            return true;
        default:
            return false;
    }
}
static inline bool ctypeIsSpace(uint32_t c) {
	switch (c) {
		case ' ':
		case CP_ZERO_WIDTH_SPACE:
			return true;
		default:
			return false;
	}
}
static inline bool ctypeIsGraphical(uint32_t c) {
    return !ctypeIsSpace(c) && !ctypeIsLineEnding(c);
}
static inline bool ctypeIsGraphemeBase(uint32_t c) {
	switch(c) {
		case CP_COMBINING_LOW_LINE:
		case CP_ZERO_WIDTH_SPACE:
			return false;
		default:
			return !ctypeIsLineEnding(c);
	}
}

/** floor(d): rounds towards -inf, consistent with libschrift's bbox rounding */
static inline int32_t wholePart(SCHRIFT_F64 d) {
    return SCHRIFT_F64_floor_to_int(d);
}
/** d - floor(d): always in [0,1), also for negative d */
static inline SCHRIFT_F64 fractionalPart(SCHRIFT_F64 d) {
    return SCHRIFT_F64_sub(d , SCHRIFT_F64_from_s32(wholePart(d)));
}

static inline void advancePen(
		T_ErrorHandler* eh, T_RenderData * rData, uint32_t prevCodepoint, uint32_t currentCodepoint, T_DPoint* pen,T_SurfaceData* surface, SCHRIFT_F64 spaceJustification)
{
    SFT_GMetrics gmtx = {0};
	SFT * sft = &rData->sft;
	//before

	SFT_Glyph glyph = qls_lookup(eh, rData, currentCodepoint);
	if (eh->code != QLS_ERROR_OK) {
		return;
	}


	if (prevCodepoint != 0) {
        SFT_Glyph prevGlyph = 0;
        sft_lookup(sft, prevCodepoint, &prevGlyph);
        if (prevGlyph == 0) {
            // inter-font kerning: best-effort (xShift is goint to be 0)
            // We can modify this later, without much worry for compatibilty
        }

		SFT_Kerning kerning;
		if (sft_kerning(sft, prevGlyph, glyph, &kerning) < 0) {
			ERROR(eh, QLS_ERROR_GLIPH_KERNING, "Invalid kerning between codepoints 0x%04X 0x%04X ", prevCodepoint, currentCodepoint);
			return;
		}
        kerning.xShift = SCHRIFT_F64_floor_to_Zper0x1p8(kerning.xShift); //arbitrary choice between floor and ceil

		SCHRIFT_F64_addEq(pen->x , rData->fontSettings->letterSpacing);
		SCHRIFT_F64_addEq(pen->x , kerning.xShift);
	}

	//render
	 sft->xOffset = fractionalPart(pen->x);
	 // libschrift yOffset is Y-up!
	 sft->yOffset =  SCHRIFT_F64_neg(fractionalPart(pen->y));
	 if (sft_gmetrics(sft, glyph, &gmtx) < 0) {
		ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X bad glyph metrics", currentCodepoint);
		return;
	 }
     gmtx.advanceWidth = SCHRIFT_F64_ceil_to_Zper0x1p8(gmtx.advanceWidth); //other properties' precision OK

	 //NOTE: sft->xOffset is read by sft_gmetrics
	 //glyphs without outline (e.g. space) have 0 area: nothing to render
	 if (surface != NULL && gmtx.minWidth > 0 && gmtx.minHeight > 0)
	 {
		 //this is the real renderer stuff. Skip this in case of layout info calculation
		SFT_Image img = {
				// no row padding: libschrift uses width as stride, copy_rect reads per byte
				.width = gmtx.minWidth,
				.height = gmtx.minHeight
		};
		// small glyphs on stack, large ones spill to heap
		// sft_render writes every pixel, no need to clean pixels
		uint8_t* pixels;
		QLS_STACK_ALLOC(pixels, uint8_t, GLYPH_STACK_BUFFER_SIZE, (size_t)img.width * (size_t)img.height);
		img.pixels = pixels;
		if (pixels == NULL) {
			ERROR(eh, QLS_ERROR_GLIPH_RENDER, "codepoint 0x%04X out of memory (%dx%d)",
					currentCodepoint, img.width, img.height);
			return;
		}

		if (sft_render(sft, glyph, img) < 0) {
			QLS_STACK_FREE(pixels);
			ERROR(eh, QLS_ERROR_GLIPH_RENDER, "codepoint 0x%04X not rendered",
					currentCodepoint);
			return;
		}

		/* pen.x = W + f, f was passed to libschrift as xOffset.
		 * Bitmap column 0 is at bbox[0] = floor(xMin*scale + f) relative to W.
		 * leftSideBearing = lsb*scale + f (already contains f), so floor(leftSideBearing) == bbox[0].
		 * Must be floor (not trunc): negative bearings (e.g. 'j') round towards -inf like libschrift does.
		 * leftSideBearing only positions the bitmap, it is NOT added to the pen (advanceWidth contains it). */
		const int32_t screenX = wholePart(pen->x)
				+ SCHRIFT_F64_floor_to_int(gmtx.leftSideBearing); //precision OK
		const int32_t screenY = wholePart(pen->y) + gmtx.yOffset;
		copy_rect(screenX, screenY, surface, &img, rData->color);
		QLS_STACK_FREE(pixels);
	}

	 //restore offset for next char
	 sft->xOffset = SCHRIFT_F64_from_s32(0);
	 sft->yOffset = SCHRIFT_F64_from_s32(0);

	 //after
	if (currentCodepoint == ' ') {
		SCHRIFT_F64_addEq(pen->x, spaceJustification);
	}
	SCHRIFT_F64_addEq(pen->x, gmtx.advanceWidth);
	return;
}

static inline void prescanLine(T_ErrorHandler* eh, T_RenderData* rData, UTF16_Reader* reader, T_Result_PrescanLine* outResult)
{
    SCHRIFT_F64 availableWidth = SCHRIFT_F64_from_s32(renderData_GetWidth(rData));
	outResult->lineStart = reader->pos;

	bool wrapchar;
	bool wrapword;
	switch(rData->wrapMode){
		case QLS_WRAP_WORD:
			wrapchar = false;
			wrapword = true;
			break;
		case QLS_WRAP_CHAR:
			wrapchar = true;
			wrapword = false;
			break;
		case QLS_WRAP_WORDCHAR:
			wrapchar = true;
			wrapword = true;
			break;
		case QLS_WRAP_NONE:
		default:
			wrapchar = false;
			wrapword = false;
			break;
	}
	T_Result_PrescanLine ret = {0};
	ret.lineStart = reader->pos;
	ret.lineEnd = reader->end;
	ret.lMetrics = rData->lineMetrics;
	T_DPoint pen = {0,0};
	uint32_t codepoint = 0;
	const uint16_t* lastSpace = NULL;
	SCHRIFT_F64 widthAtLastSpace = SCHRIFT_F64_from_s32(0);
	SFT_LMetrics metricsAtLastSpace = ret.lMetrics;
	uint32_t prevGBCodepoint = 0;
	while (reader-> pos < reader->end) {
		//position in front of `codepoint`, so a codepoint that does not belong to this line can be excluded
		const uint16_t* charStart = reader->pos;
        codepoint = utf16_read_codepoint(reader);
        if (ctypeIsLineEnding(codepoint) ) {
        	ret.lineEnd = charStart;
        	break;
        }
        if (codepoint == CP_COMBINING_LOW_LINE) {
        	//does not advance the pen in layoutAndRender either
        	continue;
        }
        if (ctypeIsSpace(codepoint)){
			if (codepoint == ' ') {
				ret.nVariableWidthSpaces++;
			}
        	lastSpace = charStart;
        	//pen is still in front of the space
        	widthAtLastSpace = pen.x;
        	metricsAtLastSpace = ret.lMetrics;
        }

		if (ctypeIsGraphemeBase(codepoint)) {
			advancePen(eh, rData, prevGBCodepoint, codepoint, &pen, NULL, SCHRIFT_F64_from_s32(0));
			if (eh->code != QLS_ERROR_OK) {
				return;
			}
		}

		if (SCHRIFT_F64_compare_lt(availableWidth, pen.x)) {
        	//current character does not fit into line
        	if (wrapword && lastSpace != NULL) {
        		ret.lineEnd = lastSpace;
				if (*lastSpace == ' ') {
				ret.nVariableWidthSpaces--;   // the break space is not part of the line
				}
        		ret.width = widthAtLastSpace;
        		//store line metrics, at the possible wrap point
        		ret.lMetrics = metricsAtLastSpace;
        		//we can wrap on last word
        		break;
        	} else if (wrapchar && charStart != ret.lineStart) {
        		//we can wrap on char boundary, but never produce an empty line
        		ret.lineEnd =  charStart;
			if (charStart == lastSpace && *lastSpace == ' ') {
        			//can happen in case of WRAP_CHAR
				ret.nVariableWidthSpaces--;   // the break space is not part of the line
        		}
        		break;
         	} else {
         		//wrap is not allowed, advance until next line break possibility
         	}
        }
		//rData->lineMetrics is the font of the codepoint just accepted
		SCHRIFT_F64 h1 = LINE_HEIGHT(ret.lMetrics);
		SCHRIFT_F64 h2 = LINE_HEIGHT(rData->lineMetrics);
		if (SCHRIFT_F64_compare_lt(h1, h2)) {
			ret.lMetrics = rData->lineMetrics;
		}
		if (ctypeIsGraphemeBase(codepoint)) {
			prevGBCodepoint = codepoint;
		}
		//kept one codepoint behind pen, so the `break`s above see the width excluding the breaking codepoint
		ret.width = pen.x;
	}
	//ret.lineEnd is the break point; skip this line's trailing whitespace and hard wrap
	//reset the reader to lineEnd
	reader->pos = ret.lineEnd;

	//drop trailing spaces; the next line's leading spaces are kept
	//peeked, not read: a consuming read would also swallow the first non-space
	while (ctypeIsSpace(utf16_peek_codepoint(reader))) {
		utf16_read_codepoint(reader);
	}
	//drop line endings, but only the first one!
	if (reader->pos < reader->end && '\r' == *reader->pos) {
		reader->pos++;
	}
	if (reader->pos < reader->end && '\n' == *reader->pos) {
		reader->pos++;
	}

	ret.nextLineStart = reader->pos;
	//NOTE: "foobar\n\0" is considered one line - since we skip first, and test after
	ret.wasLastLine = reader->end <= ret.nextLineStart;
	*outResult = ret;

#ifdef DEBUG
    SCHRIFT_F64 excessPrecision = SCHRIFT_F64_sub(pen.x, SCHRIFT_F64_floor_to_Zper0x1p8(pen.x));
    if (SCHRIFT_F64_to_double(excessPrecision) != 0.0) {
        LOG("pen.x has excess precision: %g", SCHRIFT_F64_to_double(excessPrecision));
    }
#endif
}

static inline int32_t renderData_GetWidth(T_RenderData* r) {
    return r->maxPen.x - r->minPen.x;
}
static inline int32_t getSpareWidth(int32_t boxWidth, SCHRIFT_F64 lineWidth) {
	//treat the line with wider or equal than the exact value during int conversion
	int32_t intLineWidth = SCHRIFT_F64_ceil_to_int(lineWidth);
	return boxWidth - intLineWidth;

}
static inline int32_t renderData_GetHeight(T_RenderData* r) {
    return r->maxPen.y - r->minPen.y;
}
static inline int32_t getSpareHeight(int32_t boxHeight, int32_t textHeight) {
    return boxHeight - textHeight;
}

static inline int32_t prescanText(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text, uint32_t textLen) {
	T_RenderData r2 = *r;
	//never recursive!
	r2.hAlign = HALIGN_LEFT;
	r2.vAlign = VALIGN_TOP;
	return layoutAndRender(eh, &r2, text, textLen, NULL).height;
}

static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text, uint32_t textLen, T_SurfaceData* surface) {
    /**
     * TODO
     * pen.y calculation will be complicated by the fallback-font feature
     * Should we do anything special with text-leading and text-trailing blank lines?
     * Should result.height = 0, when the whole text is blank?
     */
    T_DPoint pen = {
       SCHRIFT_F64_const_zero, SCHRIFT_F64_const_zero
    };
    switch (r->vAlign) { // pen.y += ...
        default:
        case VALIGN_TOP: {
            break;
        }
        case VALIGN_MIDDLE: {
            int32_t textHeight = prescanText(eh, r, text, textLen);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            //integer offset on purpose: the text rect lands on the same pixels as in layout
            int32_t v_offset = getSpareHeight(renderData_GetHeight(r), textHeight) / 2;
            pen.y = SCHRIFT_F64_from_s32(v_offset);
            break;
        }
        case VALIGN_BOTTOM: {
            int32_t textHeight = prescanText(eh, r, text, textLen);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            pen.y = SCHRIFT_F64_from_s32(getSpareHeight(renderData_GetHeight(r), textHeight));
            break;
        }
    }
    pen.y = SCHRIFT_F64_add(pen.y, SCHRIFT_F64_from_s32(r->minPen.y));
    SCHRIFT_F64 penStart = pen.y;
    uint_fast16_t iLine = 0;
    UTF16_Reader reader = {
		.start = text,
		.end = text + textLen,
		.pos = text };
    if (reader.end <= reader.start)
    {
        // Prevent endless loop by bad arithmetics internal error.
        ERROR(eh, QLS_ERROR_INTERNAL_READER_INDEX, "Internal error reader index");
        return (T_SizeInt) {0, 0};
    }
    T_Result_PrescanLine lineData = {0};
    SCHRIFT_F64 lineWidthMax = SCHRIFT_F64_from_s32(0);
    /**
     * do while !wasLastLine:
     *      prescan line
     *      halign: pen.x += ... ; spaceJustification = ...
     *      lineWidthMax = ...
     *      OPT render line: for (codepoint in line) advance & render
     */
    SCHRIFT_F64 lineGap = SCHRIFT_F64_const_zero;
    do {
        const uint16_t * startPos = reader.pos;
        prescanLine(eh, r, &reader, &lineData);
        if (eh->code != QLS_ERROR_OK) {
            return (T_SizeInt) {0, 0};
        }
        SFT_LMetrics * lm = &lineData.lMetrics;
        lineWidthMax = SCHRIFT_F64_max(lineWidthMax, lineData.width);
        pen.y = SCHRIFT_F64_add(pen.y, SCHRIFT_F64_add(lm->ascender, lineGap));
        pen.x = SCHRIFT_F64_from_s32(r->minPen.x);
        SCHRIFT_F64 spaceJustification = SCHRIFT_F64_from_s32(0);
        int32_t hoffset = 0;
        int32_t spareWidth  = getSpareWidth(renderData_GetWidth(r), lineData.width);
        switch (r->hAlign) { // pen.x += ... ; spaceJustification = ...; lineWidthMax = ...
            default:
            case HALIGN_LEFT: {
                break;
            }
            case HALIGN_CENTER: {
            	hoffset  = spareWidth / 2;
                break;
            }
            case HALIGN_RIGHT: {
            	hoffset  = spareWidth;
                break;
            }
            case HALIGN_JUSTIFY: {
                if (0 < lineData.nVariableWidthSpaces && 0 < spareWidth) {
                    spaceJustification = SCHRIFT_F64_div(SCHRIFT_F64_from_s32(spareWidth) , SCHRIFT_F64_cast_from_u32(lineData.nVariableWidthSpaces));
                    spaceJustification = SCHRIFT_F64_floor_to_Zper0x1p8(spaceJustification);
                    lineWidthMax = SCHRIFT_F64_max(lineWidthMax, SCHRIFT_F64_from_s32(renderData_GetWidth(r)));
                } else {
                	//copy behavior of cairo - align CENTER
					hoffset  = getSpareWidth(renderData_GetWidth(r), lineData.width) / 2;
                }
                break;
            }
        }
		SCHRIFT_F64_addEq(pen.x , SCHRIFT_F64_from_s32(hoffset));

        const SCHRIFT_F64 lineStartX = pen.x;
        SCHRIFT_F64 charStartX = pen.x; //lastCharEndX, in reality
        SCHRIFT_F64 charEndX = pen.x;

        reader.pos = lineData.lineStart;
        uint32_t lastCodepoint = 0;
        if (lineData.lineEnd>reader.end)
        {
            // Avoid endless loop in case of bad arithmetics
            ERROR(eh, QLS_ERROR_INTERNAL_LINEEND_INDEX, "Internal error lineEnd index");
            return (T_SizeInt) {0, 0};
        }
        while (reader.pos < lineData.lineEnd) { // renderGlyph(...)
            uint32_t codepoint = utf16_read_codepoint(&reader);
            switch (codepoint) {
                case CP_COMBINING_LOW_LINE: {
                    //TODO line-start combining low line, or combining low line after non-combiningbase character: get no bespoke support.
                    //TODO Single character underline: no special effort is made to make the line continuous and non-overlapping over spans of text

                    //NOTE: In case of layout, this code prevents U+332 from adding to the width, even as a missng glyph. And surface == NULL is handled.
                    render_underline(eh, r, surface, pen.y, charStartX, charEndX);
                    break;
                }
				case CP_ZERO_WIDTH_SPACE: {
					break; //could be a missing glyph
				}
                default: {
                    charStartX = pen.x; //TODO we should probably take the position AFTER we have added letter spacing
                    advancePen(eh, r, lastCodepoint, codepoint, &pen, surface, spaceJustification);
                    if (eh->code != QLS_ERROR_OK){
                        return (T_SizeInt) {0, 0};
                    }
                    charEndX = pen.x;
                    lastCodepoint = codepoint;
                    break;
                }
            }
        }

        render_underline_opt(eh, r, surface, pen.y, lineStartX, pen.x);

        iLine += 1;
        reader.pos = lineData.nextLineStart;
        pen.y = SCHRIFT_F64_sub(pen.y, lm->descender);
        lineGap = lm->lineGap;
        if (startPos>=reader.pos)
        {
            // Prevent endless loop in case reader.pos does not advance in a loop cycle. This can only happen in case of an internal error of the logic.
            ERROR(eh, QLS_ERROR_INTERNAL_ENDLESS_LOOP, "Internal error endless loop in layoutAndRender");
            return (T_SizeInt) {0, 0};
        }
    } while (!lineData.wasLastLine && reader.pos < reader.end);

    // TODO line gap of final line should be included or not? -> not sure, I think NO
    return (T_SizeInt) {
        .width = SCHRIFT_F64_ceil_to_int(lineWidthMax),
        .height = SCHRIFT_F64_ceil_to_int(SCHRIFT_F64_sub(pen.y, penStart))
    };
}


static inline uint32_t utf16_read_codepoint(UTF16_Reader* reader ) {
	uint32_t cp = '\0';
	if (reader->pos < reader->end)
	{
		cp = *reader->pos;
		reader->pos++;
		if (cp >= 0xD800 && cp <= 0xDBFF && (reader->pos < reader->end ))
		{
			uint32_t lo = *reader->pos;
			reader->pos++;
			if (lo >= 0xDC00 && lo <= 0xDFFF)
			{
				cp = 0x10000 + (((cp - 0xD800) << 10) | (lo - 0xDC00));
			}
		}
	}
	return cp;
}

static inline uint32_t utf16_peek_codepoint(UTF16_Reader* reader ) {
	const uint16_t* save = reader->pos;
	uint32_t cp = utf16_read_codepoint(reader);
	reader->pos = save;
	return cp;
}
#if INCLUDE_MAIN

int save_tga(const char *filename,
             const uint8_t *canvas,
             int width,
             int height,
             int stride)
{
    FILE *f = fopen(filename, "wb");
    if (!f)
        return 0;

    /*
     * TGA header: 18 bytes
     *
     * Byte  0: ID length
     * Byte  1: Color map type
     * Byte  2: Image type (3 = uncompressed grayscale)
     * Bytes 3-7: Color map specification
     * Bytes 8-9: X origin
     * Bytes 10-11: Y origin
     * Bytes 12-13: Width
     * Bytes 14-15: Height
     * Byte 16: Bits per pixel
     * Byte 17: Image descriptor
     */
    uint8_t header[18] = {0};

    header[2] = 3;                         // Uncompressed grayscale
    header[12] = (uint8_t)(width & 0xff);
    header[13] = (uint8_t)((width >> 8) & 0xff);
    header[14] = (uint8_t)(height & 0xff);
    header[15] = (uint8_t)((height >> 8) & 0xff);
    header[16] = 8;                        // 8 bits/pixel

    /*
     * Image descriptor:
     * bit 5 = 0 -> origin is bottom-left.
     *
     * This means we write the canvas starting from its last row.
     */
    header[17] = 0x00;

    if (fwrite(header, 1, sizeof(header), f) != sizeof(header)) {
        fclose(f);
        return 0;
    }

    // TGA with bottom-left origin: write rows bottom-to-top.
    for (int y = height - 1; y >= 0; --y) {
        if (fwrite(canvas + y * stride, 1, (size_t)width, f) != (size_t)width) {
            fclose(f);
            return 0;
        }
    }

    fclose(f);
    return 1;
}

int main() {
    puts("Hello World!");
    uint16_t message[256] = {0};

    uint32_t l = 0;
    uint32_t max = sizeof(message) / sizeof (uint16_t);
    {
        const unsigned char helloworld[] = "Hello World!  \nHello world!";
        uint16_t* writer = message;
		for (int i = 0; helloworld[i] != '\0' && l < max; ++i) {
			*writer++ = helloworld[i];
			l++;
		}
    }

    enum { HEIGHT = 50, WIDTH = 80, STRIDE = ((WIDTH + 3) & ~3)};
    uint8_t canvas[HEIGHT * STRIDE] = {0};
    T_SurfaceData surface = {
        .data = canvas,
        .height = HEIGHT,
        .width = WIDTH
    };

    T_ErrorHandler eh = {0};
    T_TrueTypeFont* fonts[1] = {0};
    enum { PX_SIZE = 8 };
    T_FontSettings fontSettings = {
        .fontsCount = 1,
        .fonts = fonts,
        .fontSize = SCHRIFT_F64_from_s32(PX_SIZE),
        .letterSpacing = SCHRIFT_F64_const_zero
    };
    T_TrueTypeFont font = {
        .ttfFilePath = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    };
    fonts[0] = &font;
    qls_load_font(&eh, fonts[0]);

    T_RenderData r = {
        .font = fonts[0],
        .fontSettings = &fontSettings,
        .color = ~(uint32_t)0,
        .minPen = {0, 0},
        .maxPen = {WIDTH, HEIGHT},
        .wrapMode = QLS_WRAP_WORD,
        .hAlign = HALIGN_RIGHT,
        .vAlign = VALIGN_BOTTOM,
    };

    qls_apply_font(&eh,&r, 0);

    layoutAndRender(&eh, &r, message,l, &surface);

    for (int x = 0; x < WIDTH; ++x) {
        putchar('-');
    }
    putchar('\n');
    for (int y = 0; y < HEIGHT; ++y) {
        for (int x = 0; x < WIDTH; ++x) {
            char c = " .:ioVM@"[canvas[y * STRIDE + x] >> 5];
            putchar(c);
        }
        putchar('\n');
    }
    for (int x = 0; x < WIDTH; ++x) {
        putchar('-');
    }
    
    save_tga("/tmp/out.tga", canvas, WIDTH, HEIGHT, STRIDE);

    return eh.code;
}

#endif // INCLUDE_MAIN
