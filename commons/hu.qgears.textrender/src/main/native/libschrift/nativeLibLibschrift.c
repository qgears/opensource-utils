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
    SFT_LMetrics lineMetrics;
    T_IPoint minPen;
    T_IPoint maxPen;
    uint32_t color;
    SCHRIFT_F64 letterSpacing;
    uint32_t wrapMode;
    uint32_t hAlign;
    uint32_t vAlign;
} T_RenderData;

typedef struct {
    uint32_t nSpaces;
    const uint16_t* lineStart;
    //exclusive, consistent with `len`
    const uint16_t* lineEnd;
    //inclusive
    const uint16_t* nextLineStart;
    bool wasLastLine;
    SCHRIFT_F64 width;
} T_Result_PrescanLine;

typedef struct {
    SCHRIFT_F64 textHeight;
} T_Result_PrescanText;

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

#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define LIMIT(x,min,max) ((x) < (min) ? (min) : ((x) > (max) ? (max) : (x)))
#define PIX(r)  SCHRIFT_F32_trunc_to_u8_to_u32((SCHRIFT_F32_mul_u32(r , 0xFFu)))
#define LOG(...) ;printf(__VA_ARGS__);printf("\n");fflush(stdout)

static inline uint32_t blend_bgra_premultiplied(uint32_t dst, uint32_t pcolor, uint8_t mask);
static inline void copy_rect(int32_t startx, int32_t starty, T_SurfaceData* surface, SFT_Image* img, uint32_t color);
static void qls_load_font(T_ErrorHandler* eh,T_RenderData* r, T_TrueTypeFont* font);
static T_SurfaceData* qls_get_surfacedata(uint64_t id);
static inline void advancePen(
		T_ErrorHandler* eh, T_RenderData * rData, uint32_t prevCodepoint, uint32_t currentCodepoint, T_DPoint* pen,T_SurfaceData* surface, SCHRIFT_F64 spaceJustification);
/**
 * Stops advancing at the terminating '\0'.
 */
static inline uint32_t utf16_read_codepoint(UTF16_Reader* reader );
/**
 * Same as utf16_read_codepoint, but leaves the reader position unchanged.
 */
static inline uint32_t utf16_peek_codepoint(UTF16_Reader* reader );
static inline bool ctypeIsLineEnding(uint32_t c);
static inline bool ctypeIsSpace(uint32_t c);
static inline bool ctypeIsGraphical(uint32_t c);
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
static inline void prescanText(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text,uint32_t textLen, T_Result_PrescanText* outResult);
static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text,uint32_t textLen, T_SurfaceData* surface);

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
    T_SurfaceData* surfaceData = (T_SurfaceData*)surfaceHandle;
    free(surfaceData);
}

T_SizeInt qls_renderTextPrivate(T_ErrorHandler* errorHandler, uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text, uint32_t textLen,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            SCHRIFT_F32 r, SCHRIFT_F32 g, SCHRIFT_F32 b, SCHRIFT_F32 a, bool clip, uint32_t wrapMode)
{
    T_SizeInt result = {0, 0};
    T_SurfaceData* surface = qls_get_surfacedata(surfaceHandle);
    if (surface) {
        uint32_t* surfaceData = (uint32_t*)surface->data;
        if (surfaceData) 
        {
            T_RenderData rData = {0};
            rData.minPen.x = x;
            rData.minPen.y = y;
            rData.maxPen.x = x + width;
            rData.maxPen.y = y + height;
            rData.letterSpacing = font->letterSpacing;
            rData.wrapMode = wrapMode;
            rData.hAlign = hAlign;
            rData.vAlign = vAlign;
            if (rData.maxPen.y > rData.minPen.y && rData.maxPen.x > rData.minPen.x)
            {
                qls_load_font(errorHandler, &rData,font);
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

T_SizeInt qls_layoutTextPrivate(T_ErrorHandler* errorHandler, T_TrueTypeFont* font, const uint16_t* text, 
                            uint32_t textLen, uint32_t hAlign, int32_t width, uint32_t wrapMode)
{
    T_SizeInt result = {0,0};
    T_RenderData r = {0};
    r.maxPen.x = width;
    r.maxPen.y = INT32_MAX;
    r.sft.flags = SFT_DOWNWARD_Y;
    r.letterSpacing = font->letterSpacing;
    r.wrapMode = wrapMode;
    r.hAlign = hAlign;
    enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM }; // TODO single source of truth
    r.vAlign = VALIGN_TOP;
    qls_load_font(errorHandler,&r,font);
    if (errorHandler->code == QLS_ERROR_OK)
    {
        result = layoutAndRender(errorHandler, &r, text,textLen, NULL);
        // if (errorHandler->code == QLS_ERROR_OK)
        // {
        // }
    }
    return result;
}

/*********************************************/
/***   Static function implementations     ***/
/*********************************************/
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
    for (j = min_src_x,dstj = startx+min_src_x; j < max_src_x; j++, dstj++ ) {
        for (i = min_src_y, dsti = starty+min_src_y; i < max_src_y; i++, dsti++ ) {
            int32_t dst_pix = (dsti*rowStride) + dstj;
            int32_t src_pix = (i*img->width) + j;
            uint8_t src_alpha = imageData[src_pix];
            if (src_alpha > 0) {  // Only copy non-transparent pixels
                if (surface->color) {
                    surfaceData4[dst_pix] = blend_bgra_premultiplied(surfaceData4[dst_pix], color,src_alpha);
                } else {
                    uint32_t da = surfaceData[dst_pix];
                    surfaceData[dst_pix] = (uint8_t)(src_alpha + da * (255 - src_alpha) / 255);
                }
            }
        }
    }
}

static void qls_load_font(T_ErrorHandler* eh, T_RenderData* r, T_TrueTypeFont* font) {
    SFT* sft = &(r->sft);
    sft->xScale = SCHRIFT_F64_from_SCHRIFT_F32(font->fontSize);
    sft->yScale = SCHRIFT_F64_from_SCHRIFT_F32(font->fontSize);
    sft->flags = SFT_DOWNWARD_Y;

    if (font->font == NULL)
    {
        //lazy init font
        font->font = sft_loadfile(font->ttfFilePath);
        if (font->font == NULL)
        {
            ERROR(eh,QLS_ERROR_FONT_LOAD, "TTF load failed %s" , filename(font->ttfFilePath));
            return;
        }
        else
        {
            sft->font = font->font;
		    if (sft_lmetrics(sft,&(font->lineMetrics)) < 0)
            {
		        ERROR(eh,QLS_ERROR_LINE_METRICS, "Failed to init line metrics of font %s" , filename(font->ttfFilePath));
		        return;
		    } else {
		        LOG("LineMetrics of %s asc %lf, desc %lf, gap %lf",
                    filename(font->ttfFilePath),
                    SCHRIFT_F64_to_double(font->lineMetrics.ascender),
                    SCHRIFT_F64_to_double(font->lineMetrics.descender),
                    SCHRIFT_F64_to_double(font->lineMetrics.lineGap));
		    }
		}
    }
    sft->font = font->font;
    r->lineMetrics = font->lineMetrics;
}

static T_SurfaceData* qls_get_surfacedata(uint64_t id) {
    // Cast the handle back to T_SurfaceData pointer
    return (T_SurfaceData*)id;
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
    return c == ' ';
}
static inline bool ctypeIsGraphical(uint32_t c) {
    return !ctypeIsSpace(c) && !ctypeIsLineEnding(c);
}

static inline int32_t wholePart(SCHRIFT_F64 d) {
    // TODO optimize wholePart/fractionalPart counting of SOFTFLOAT
    return SCHRIFT_F64_trunc_cast_to_s32( d);
}
static inline SCHRIFT_F64 fractionalPart(SCHRIFT_F64 d) {
    // TODO optimize wholePart/fractionalPart counting of SOFTFLOAT
    return SCHRIFT_F64_sub(d , SCHRIFT_F64_from_s32(wholePart(d)));
}

static inline void advancePen(
		T_ErrorHandler* eh, T_RenderData * rData, uint32_t prevCodepoint, uint32_t currentCodepoint, T_DPoint* pen,T_SurfaceData* surface, SCHRIFT_F64 spaceJustification)
{
	SFT_Glyph glyph = 0;
    SFT_GMetrics gmtx = {0};
	SFT * sft = &rData->sft;
	//before
	if (sft_lookup(sft, currentCodepoint, &glyph) < 0) {
		ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", currentCodepoint);
		return;
	}
	if (prevCodepoint != 0) {
		SFT_Glyph gPrev;
		if (sft_lookup(sft, prevCodepoint, &gPrev) < 0) {
			ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", prevCodepoint);
			return;
		}
		SFT_Kerning kerning;
		if (sft_kerning(sft, gPrev, glyph, &kerning) < 0) {
			ERROR(eh, QLS_ERROR_GLIPH_KERNING, "Invalid kerning between codepoints 0x%04X 0x%04X ", prevCodepoint, currentCodepoint);
			return;
		}
		SCHRIFT_F64_addEq(pen->x , rData->letterSpacing);
		SCHRIFT_F64_addEq(pen->x , kerning.xShift);
	}
	//render
	 sft->xOffset = fractionalPart(pen->x);
	 sft->yOffset = fractionalPart(pen->y);
	 if (sft_gmetrics(sft, glyph, &gmtx) < 0) {
		ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X bad glyph metrics", currentCodepoint);
		return;
	 }

	 //NOTE: sft->xOffset is read by sft_gmetrics
	 if (surface != NULL)
	 {
		 //this is the real renderer stuff. Skip this in case of layout info calculation
		SFT_Image img = {
				.width = (gmtx.minWidth + 3) & ~3, //TODO consider aligning to 8?
				.height = gmtx.minHeight
		};
		// VLA! TODO allocate variable array on stack?
		uint8_t pixels[img.width * img.height];
		// memset 0 is called by sft_render on array, no need to clean pixels
		img.pixels = pixels;

		if (sft_render(sft, glyph, img) < 0) {
			ERROR(eh, QLS_ERROR_GLIPH_RENDER, "codepoint 0x%04X not rendered",
					currentCodepoint);
			return;
		}

		const int32_t screenX = wholePart(pen->x)
				+ wholePart(gmtx.leftSideBearing);
		const int32_t screenY = wholePart(pen->y) + gmtx.yOffset;
		copy_rect(screenX, screenY, surface, &img, rData->color);
	}

	 //restore offset for next char
	 sft->xOffset = SCHRIFT_F64_from_s32(0);
	 sft->yOffset = SCHRIFT_F64_from_s32(0);

	 //after
	if (ctypeIsSpace(currentCodepoint)) {
		SCHRIFT_F64_addEq(pen->x, spaceJustification);
	}
	SCHRIFT_F64_addEq(pen->x, gmtx.advanceWidth);
}

static inline bool prescanLine_IsLineTooWide(SCHRIFT_F64 availableWidth, uint_fast16_t l, SCHRIFT_F64 w) {
    return 1 <= l && SCHRIFT_F64_compare_lt(availableWidth , w);
}

static inline void prescanLine(T_ErrorHandler* eh, T_RenderData* rData, UTF16_Reader* reader, T_Result_PrescanLine* outResult)
{
	SCHRIFT_F64 letterSpacing = rData->letterSpacing;
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
	T_DPoint pen = {0,0};
	uint32_t prevCodepoint = 0;
	uint32_t codepoint = 0;
	const uint16_t* lastSpace = NULL;
	SCHRIFT_F64 widthAtLastSpace = SCHRIFT_F64_from_s32(0);
	while (reader-> pos < reader->end) {
		//position in front of `codepoint`, so a codepoint that does not belong to this line can be excluded
		const uint16_t* charStart = reader->pos;
        codepoint = utf16_read_codepoint(reader);
        if (ctypeIsLineEnding(codepoint) ) {
        	ret.lineEnd = charStart;
        	break;
        }
        if (ctypeIsSpace(codepoint)){
        	ret.nSpaces++;
        	lastSpace = charStart;
        	//pen is still in front of the space
        	widthAtLastSpace = pen.x;
        }
		advancePen(eh, rData, prevCodepoint, codepoint, &pen, NULL, SCHRIFT_F64_from_s32(0));
		if (eh->code != QLS_ERROR_OK) {
			return;
		}

		if (SCHRIFT_F64_compare_lt(availableWidth, pen.x)) {
        	//current character does not fit into line
        	if (wrapword && lastSpace != NULL) {
        		ret.lineEnd = lastSpace;
        		ret.nSpaces--;   // the break space is not part of the line
        		ret.width = widthAtLastSpace;
        		//we can wrap on last word
        		break;
        	} else if (wrapchar && charStart != ret.lineStart) {
        		//we can wrap on char boundary, but never produce an empty line
        		ret.lineEnd =  charStart;
        		if (charStart == lastSpace) {
        			//can happen in case of WRAP_CHAR
        			ret.nSpaces--;   // the break space is not part of the line
        		}
        		break;
         	} else {
         		//wrap is not allowed, advance until next line break possibility
         	}
        }
		prevCodepoint = codepoint;
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
}

static inline int32_t renderData_GetWidth(T_RenderData* r) {
    return r->maxPen.x - r->minPen.x;
}
static inline SCHRIFT_F64 getSpareWidth(int32_t boxWidth, SCHRIFT_F64 lineWidth) {
    return SCHRIFT_F64_sub(SCHRIFT_F64_from_s32(boxWidth) , lineWidth);
}
static inline int32_t renderData_GetHeight(T_RenderData* r) {
    return r->maxPen.y - r->minPen.y;
}
static inline SCHRIFT_F64 getSpareHeight(int32_t boxHeight, SCHRIFT_F64 textHeight) {
    return SCHRIFT_F64_sub(SCHRIFT_F64_from_s32(boxHeight) , textHeight);
}

static inline void prescanText(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text, uint32_t textLen, T_Result_PrescanText* outResult) {
    uint_fast16_t nLines = 0;
	UTF16_Reader reader = {
			.start = text,
			.end = text + textLen,
			.pos = text };
    T_Result_PrescanLine lineData = {0};
    do {
        prescanLine(eh, r, &reader, &lineData);
        if (eh->code != QLS_ERROR_OK) {
            return;
        }
        nLines += 1;
        //reader = lineData.nextLineStart;
    } while (!lineData.wasLastLine);

    outResult->textHeight = SCHRIFT_F64_sub(SCHRIFT_F64_mul(SCHRIFT_F64_from_u16_fast(nLines) , (SCHRIFT_F64_add(SCHRIFT_F64_sub(r->lineMetrics.ascender , r->lineMetrics.descender) , r->lineMetrics.lineGap))) , r->lineMetrics.lineGap);
}

static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, T_RenderData* r, const uint16_t* text, uint32_t textLen, T_SurfaceData* surface) {
    /**
     * TODO
     * pen.y calculation will be complicated by the fallback-font feature
     * Should we do anything special with text-leading and text-trailing blank lines?
     * Should result.height = 0, when the whole text is blank?
     */
    T_DPoint pen = {
        .y = SCHRIFT_F64_add(SCHRIFT_F64_from_s32(r->minPen.y) , r->lineMetrics.ascender)
    };
    
    enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM };
    switch (r->vAlign) { // pen.y += ...
        default:
        case VALIGN_TOP: {
            break;
        }
        case VALIGN_MIDDLE: {
            T_Result_PrescanText textData = {0};
            prescanText(eh, r, text,textLen, &textData);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            SCHRIFT_F64_addEq(pen.y , SCHRIFT_F64_div(getSpareHeight(renderData_GetHeight(r), textData.textHeight) , SCHRIFT_F64_from_s32(2)));
            break;
        }
        case VALIGN_BOTTOM: {
            T_Result_PrescanText textData = {0};
            prescanText(eh, r, text,textLen, &textData);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            SCHRIFT_F64_addEq(pen.y , getSpareHeight(renderData_GetHeight(r), textData.textHeight));
            break;
        }
    }

    uint_fast16_t iLine = 0;
    UTF16_Reader reader = {
		.start = text,
		.end = text + textLen,
		.pos = text };
    T_Result_PrescanLine lineData = {0};
    SCHRIFT_F64 lineWidthMax = SCHRIFT_F64_from_s32(0);
    /**
     * do while !wasLastLine:
     *      prescan line
     *      halign: pen.x += ... ; spaceJustification = ...
     *      lineWidthMax = ...
     *      OPT render line: for (codepoint in line) advance & render
     */

    do {
        prescanLine(eh, r, &reader, &lineData);
        if (eh->code != QLS_ERROR_OK) {
            return (T_SizeInt) {0, 0};
        }
        lineWidthMax = SCHRIFT_F64_max(lineWidthMax, lineData.width);

        pen.x = SCHRIFT_F64_from_s32(r->minPen.x);
        SCHRIFT_F64 spaceJustification = SCHRIFT_F64_from_s32(0);
        enum { HALIGN_LEFT, HALIGN_MIDDLE, HALIGN_RIGHT, HALIGN_JUSTIFY }; //TODO single source of truth
        switch (r->hAlign) { // pen.x += ... ; spaceJustification = ...; lineWidthMax = ...
            default:
            case HALIGN_LEFT: {
                break;
            }
            case HALIGN_MIDDLE: {
                SCHRIFT_F64_addEq(pen.x , SCHRIFT_F64_div(getSpareWidth(renderData_GetWidth(r), lineData.width) , SCHRIFT_F64_from_s32(2)));
                break;
            }
            case HALIGN_RIGHT: {
                SCHRIFT_F64_addEq(pen.x , getSpareWidth(renderData_GetWidth(r), lineData.width));
                break;
            }
            case HALIGN_JUSTIFY: {
                if (0 < lineData.nSpaces && SCHRIFT_F64_compare_lt(SCHRIFT_F64_from_s32(0) , getSpareWidth(renderData_GetWidth(r), lineData.width))) {
                    spaceJustification = SCHRIFT_F64_div(getSpareWidth(renderData_GetWidth(r), lineData.width) , SCHRIFT_F64_from_u16_fast(lineData.nSpaces));
                    lineWidthMax = SCHRIFT_F64_max(lineWidthMax, SCHRIFT_F64_from_s32(renderData_GetWidth(r)));
                }
                break;
            }
        }


        reader.pos = lineData.lineStart;
        uint32_t lastCodepoint = 0;
        while (reader.pos < lineData.lineEnd) { // renderGlyph(...)
            uint32_t codepoint = utf16_read_codepoint(&reader);
            advancePen(eh, r,  lastCodepoint, codepoint, &pen, surface, spaceJustification);
            if (eh->code != QLS_ERROR_OK){
            	return (T_SizeInt) {0, 0};
            }
            lastCodepoint = codepoint;
        }

        iLine += 1;
        reader.pos = lineData.nextLineStart;
        SCHRIFT_F64_addEq(pen.y , SCHRIFT_F64_add(SCHRIFT_F64_sub(r->lineMetrics.ascender , r->lineMetrics.descender) , r->lineMetrics.lineGap));
    } while (!lineData.wasLastLine);

    	// TODO should not sizes be rounded up instead of just casting to int?
    return (T_SizeInt) {
        .height = SCHRIFT_F64_cast_to_s32(SCHRIFT_F64_sub(SCHRIFT_F64_mul(SCHRIFT_F64_from_u16_fast(iLine) , (SCHRIFT_F64_add(SCHRIFT_F64_sub(r->lineMetrics.ascender , r->lineMetrics.descender) , r->lineMetrics.lineGap))) , r->lineMetrics.lineGap)),
        .width = SCHRIFT_F64_cast_to_s32(lineWidthMax)
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

    enum { HALIGN_LEFT, HALIGN_MIDDLE, HALIGN_RIGHT, HALIGN_JUSTIFY };
    enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM };
    T_RenderData r = {
        .color = ~(uint32_t)0,
        .minPen = {0, 0},
        .maxPen = {WIDTH, HEIGHT},
        .wrapMode = QLS_WRAP_WORD,
        .letterSpacing = SCHRIFT_F64_from_s32(1),
        .hAlign = HALIGN_RIGHT,
        .vAlign = VALIGN_BOTTOM
    };

    enum { PX_SIZE = 8 };
    r.sft.flags |= SFT_DOWNWARD_Y;
    r.sft.xScale = SCHRIFT_F64_from_s32(PX_SIZE);
    r.sft.yScale = SCHRIFT_F64_from_s32(PX_SIZE);
    r.sft.font = sft_loadfile("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");

    sft_lmetrics(&r.sft, &r.lineMetrics);

    T_ErrorHandler eh = {0};


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
