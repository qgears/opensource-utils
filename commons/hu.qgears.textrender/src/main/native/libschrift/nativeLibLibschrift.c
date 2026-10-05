#include "nativeLibLibschrift.h"
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "util.h"

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
    double x;
    double y;
} T_DPoint;

typedef struct {
    SFT sft;
    SFT_LMetrics lineMetrics;
    T_DPoint pen;
    T_IPoint minPen;
    T_IPoint maxPen;
    T_IPoint lExtentMin;
    T_IPoint lExtentMax;
    uint32_t color;
    double letterSpacing;
    /**
     * Store prev gliph to support kerning
     */
    SFT_Glyph prev_gliph;
    uint32_t wrapMode;
    uint32_t hAlign;
    uint32_t vAlign;
} T_RenderData;

typedef struct {
    uint_fast16_t len, nSpaces;
    //exclusive, consistent with `len`
    const uint16_t* lineEnd;
    //inclusive
    const uint16_t* nextLineStart;
    bool wasLastLine;
    double width;
} T_Result_PrescanLine;

typedef struct {
    double textHeight;
} T_Result_PrescanText;

typedef enum {
    QLS_WRAP_CHAR,
    QLS_WRAP_WORD,
    QLS_WRAP_WORDCHAR,
    QLS_WRAP_NONE
} E_QLS_WRAP;

#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define LIMIT(x,min,max) ((x) < (min) ? (min) : ((x) > (max) ? (max) : (x)))
#define PIX(r)  (uint32_t)(((uint8_t)(r * 0xFFu)))
#define LOG(...) ;printf(__VA_ARGS__);printf("\n");fflush(stdout)

static int32_t lineHeightLogical(T_RenderData* rData);
static uint32_t nextWhiteSpace(const uint16_t* text,uint32_t wStart,uint32_t textLen);
static uint32_t qls_layoutAndRenderTextPart(T_ErrorHandler* errorHandler, T_RenderData* rData,T_SurfaceData* surface, const uint16_t* text, uint32_t textLen, bool allowCharWrap);
static uint32_t qls_layoutAndRender(T_ErrorHandler* errorHandler, T_RenderData* rData,T_SurfaceData* surface, const uint16_t* text, uint32_t textLen);
static inline void qls_render_gliph(T_ErrorHandler* eh, T_RenderData * rData, uint32_t cp,T_SurfaceData* surface, bool allowCharWrap);
static void qls_align(T_ErrorHandler* errorHandler, T_RenderData* rData, uint32_t hAlign, uint32_t vAlign,const uint16_t* text, uint32_t textLen);
static inline uint32_t blend_bgra_premultiplied(uint32_t dst, uint32_t pcolor, uint8_t mask);
static inline void copy_rect(int32_t startx, int32_t starty, T_SurfaceData* surface, SFT_Image* img, uint32_t color);
static void qls_load_font(T_ErrorHandler* eh,T_RenderData* r, T_TrueTypeFont* font);
static void get_font_file(T_ErrorHandler* eh, T_TrueTypeFont* font, char* filePath, uint32_t filePathLength);
static T_SurfaceData* qls_get_surfacedata(uint64_t id);
static inline int32_t dToI (double d);

/**
 * Stops advancing at the terminating '\0'.
 */
static inline uint32_t utf16Read(const uint16_t* restrict * text);
static inline void utf16Skip(const uint16_t* restrict * text);
static inline uint32_t utf16Peek(const uint16_t* restrict text);
static inline bool ctypeIsLineEnding(uint32_t c);
static inline bool ctypeIsSpace(uint32_t c);
static inline bool ctypeIsGraphical(uint32_t c);
static inline int32_t wholePart(double d);
static inline double fractionalPart(double d);
/**
 * Pass 0 for c1, if there is no previous character.
 */
static inline void advancePenBeforeRender(T_ErrorHandler* eh, const SFT* sft, double letterSpacing, uint32_t c1, uint32_t c2, double* penx);
static inline void renderGlyph(T_ErrorHandler* eh, SFT* sft, const T_DPoint* pen, uint32_t color, uint32_t codepoint, T_SurfaceData* surface);
static inline void advancePenAfterRender(T_ErrorHandler* eh, const SFT* sft, double spaceJustification, uint32_t codepoint, double* penx);
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
static inline void prescanLine(T_ErrorHandler* eh, SFT sft, const uint16_t* restrict const text, E_QLS_WRAP wrapMode, double availableWidth, double letterSpacing
        , T_Result_PrescanLine* outResult);
static inline void prescanText(T_ErrorHandler* eh, const T_RenderData* r, const uint16_t* restrict const text, T_Result_PrescanText* outResult);
static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, const T_RenderData* r, const uint16_t* restrict const text, T_SurfaceData* surface);

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
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
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
            if (rData.maxPen.y > rData.minPen.y && rData.maxPen.x > rData.minPen.x)
            {
                qls_load_font(errorHandler, &rData,font);
                if (errorHandler->code == QLS_ERROR_OK)
                {
                    qls_align(errorHandler,&rData,hAlign,vAlign,text,textLen);
                    rData.color = PIX(r) | (PIX(g) << 8) | (PIX(b) << 16) | (PIX(a) << 24);
                    qls_layoutAndRender(errorHandler,&rData,surface,text,textLen);
    
                    result.width = dToI(rData.lExtentMax.x-rData.lExtentMin.x);
                    result.height = dToI(rData.lExtentMax.y+rData.lExtentMin.y);
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
    qls_load_font(errorHandler,&r,font);
    if (errorHandler->code == QLS_ERROR_OK)
    {
        uint32_t numCodePoints = qls_layoutAndRender(errorHandler,&r,NULL,text,textLen);
        if (errorHandler->code == QLS_ERROR_OK)
        {
            if (hAlign == 3 && numCodePoints > 1) { //JUSTIFY, and at least two chars
                result.width = width;
            } else {
                result.width = dToI(r.lExtentMax.x-r.lExtentMin.x);
            }
            result.height = dToI(r.lExtentMax.y+r.lExtentMin.y);
        }
    }
    return result;
}

/*********************************************/
/***   Static function implementations     ***/
/*********************************************/
static int32_t lineHeightLogical(T_RenderData* rData)
{
    //TODO strange algorithm, tried to reverse engineer cairo's behaviour, but I'm not sure...
    return dToI(ceil(rData->lineMetrics.ascender - rData->lineMetrics.descender +rData->lineMetrics.lineGap));
}

static uint32_t nextWhiteSpace(const uint16_t* text,uint32_t wStart,uint32_t textLen) {

    for (uint32_t i = wStart+1; i <textLen; i++){
        switch (text[i])
        {
            case ' ':
            case '\n':
            case '\t':
                return i;
        }
    }
    return textLen;
}
static uint32_t qls_layoutAndRenderTextPart(T_ErrorHandler* errorHandler, T_RenderData* rData,T_SurfaceData* surface, const uint16_t* text, uint32_t textLen, bool allowCharWrap) {
    uint32_t numCodepoints = 0;
    for (uint32_t i = 0; i < textLen && (errorHandler->code == QLS_ERROR_OK); i++)
    {
        //The unicode codepoint
        uint32_t cp = text[i];
        if (cp >= 0xD800 && cp <= 0xDBFF && i + 1 < textLen)
        {
            uint32_t lo = text[i + 1];
            if (lo >= 0xDC00 && lo <= 0xDFFF)
            {
                cp = 0x10000 + (((cp - 0xD800) << 10) | (lo - 0xDC00));
                ++i;
            }
        }
        qls_render_gliph(errorHandler, rData, cp,surface,allowCharWrap);
        numCodepoints++;
    }
    return numCodepoints;
}
static uint32_t qls_layoutAndRender(T_ErrorHandler* errorHandler, T_RenderData* rData,T_SurfaceData* surface, const uint16_t* text, uint32_t textLen) {
    uint32_t numCodepoints = 0;
    if (rData->maxPen.y > rData->minPen.y && rData->maxPen.x > rData->minPen.x)
    {
        rData->lExtentMin.y = dToI(rData->pen.y);
        rData->lExtentMax.y = dToI(rData->pen.y) + lineHeightLogical(rData);
        bool allowCharWrap;
        bool layoutWordByWord;
        switch (rData->wrapMode)
        {
        case QLS_WRAP_CHAR:  //CHAR
            allowCharWrap = true;
            layoutWordByWord = false;
            break;
        case QLS_WRAP_WORD:  //WORD
            allowCharWrap = false;
            layoutWordByWord = true;
            break;
        case QLS_WRAP_NONE:  //NONE - for internal use only
            allowCharWrap = false;
            layoutWordByWord = false;
            break;
        case QLS_WRAP_WORDCHAR:  //WORDCHAR
        default:
            /* code */  
            allowCharWrap = true;
            layoutWordByWord = true;
            break;
        }
        if (layoutWordByWord)
        {
            uint32_t wStart = 0;
            uint32_t wSpace = nextWhiteSpace(text,wStart,textLen);
            if (wSpace == textLen)
            {
                //render normally
                numCodepoints = qls_layoutAndRenderTextPart(errorHandler,rData,surface,text,textLen,allowCharWrap);
            } else {
                while (wStart < textLen)
                {
                    T_RenderData rCopy = *rData;
                    qls_layoutAndRenderTextPart(errorHandler,&rCopy,NULL,&text[wStart],wSpace-wStart,true);
                    bool fitsInLine = rCopy.lExtentMax.y == rData->lExtentMax.y;
                    if (!fitsInLine)
                    {
                        rData->pen.x = rData->minPen.x;
                        rData->pen.y += lineHeightLogical(rData);
                        rData->lExtentMax.y += lineHeightLogical(rData);
                        if (!fitsInLine && wStart > 0 && wStart < (textLen -1)){
                            //skip the starting whitespace
                            wStart++;
                        }
                    }
                    numCodepoints += qls_layoutAndRenderTextPart(errorHandler,rData,surface,&text[wStart],wSpace-wStart,allowCharWrap);
                    wStart = wSpace;
                    wSpace = nextWhiteSpace(text,wStart,textLen);
                }
            }
        }
        else
        {
            //render normally
            numCodepoints = qls_layoutAndRenderTextPart(errorHandler,rData,surface,text,textLen,allowCharWrap);
        }
    }
    else
    {
        //specified target rectangle is invalid or empty
    }
    return numCodepoints;
}

static inline void qls_render_gliph(T_ErrorHandler* eh, T_RenderData * rData, uint32_t cp,T_SurfaceData* surface,bool allowCharWrap)
{

	SFT_Glyph gid;  //  unsigned long gid;
    SFT* sft = &(rData->sft);
	if (sft_lookup(sft, cp, &gid) < 0)
    {
		ERROR(eh,QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing",cp);
        return;
    }
    
	SFT_GMetrics mtx;
	if (sft_gmetrics(sft, gid, &mtx) < 0)
    {
        ERROR(eh,QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X bad glyph metrics",cp);
        return;
    }
    SFT_Kerning kerning;
    if (sft_kerning(sft,rData->prev_gliph,gid,&kerning) < 0){
        ERROR(eh,QLS_ERROR_GLIPH_KERNING, "Invalid kerning between gliphs 0x%016lX 0x%016lX ",rData->prev_gliph,gid);
        return;

    }
    bool render = (surface != NULL);

    //TODO : investigate kerning in case of "ve". The letter 'e' is rendered off by one pixel compared to cairo
    double dX = mtx.leftSideBearing +kerning.xShift;
    double dY = mtx.yOffset + kerning.yShift + rData->lineMetrics.ascender;
    int32_t x = dToI(rData->pen.x + dX);
    int32_t y = dToI(rData->pen.y + dY);
    
    if (allowCharWrap) {
        bool charFitsInLine = (x+mtx.minWidth) < rData->maxPen.x;
        if (!charFitsInLine) {
            rData->pen.x = rData->minPen.x;
            rData->pen.y += lineHeightLogical(rData);
            x = dToI(rData->pen.x + mtx.leftSideBearing);
            y = dToI(rData->pen.y + mtx.yOffset +rData->lineMetrics.ascender);
            kerning.xShift = 0;
            kerning.yShift = 0;
            rData->lExtentMax.y += lineHeightLogical(rData);
        }
    }

    rData->lExtentMin.x = MIN(x,rData->lExtentMin.x);
    //TODO allow xOffSet and yOffset
    //sft->xOffset = rData->pen.x + dX - x;
    //sft->yOffset = rData->pen.y + dY - y;
    if (render)
    {
        SFT_Image img = {
            .width  = (mtx.minWidth + 3) & ~3,
            .height = mtx.minHeight,
        };
        char pixels[img.width * img.height];
        img.pixels = pixels;
        if (sft_render(sft, gid, img) < 0)
        {
            ERROR(eh,QLS_ERROR_GLIPH_RENDER, "codepoint 0x%04X not rendered",cp);
            return;
        }
        
        copy_rect(dToI(x) ,dToI(y), surface,&img,rData->color);
    }
    rData->lExtentMax.x = MAX(dToI( rData->pen.x+mtx.advanceWidth),rData->lExtentMax.x);
    rData->pen.x += mtx.advanceWidth + kerning.xShift + rData->letterSpacing;
    rData->prev_gliph = gid;
}

static void qls_align(T_ErrorHandler* errorHandler, T_RenderData* rData, uint32_t hAlign, uint32_t vAlign,const uint16_t* text, uint32_t textLen) 
 {
    int32_t width = rData->maxPen.x - rData->minPen.x;
    int32_t height = rData->maxPen.y - rData->minPen.y;
    uint32_t numCodepoints = qls_layoutAndRender(errorHandler,rData,NULL,text,textLen);
    int32_t lExtW = rData->lExtentMax.x - rData->lExtentMin.x;
    int32_t lExtH = rData->lExtentMax.y - rData->lExtentMin.y;
    //put pen to baseline
    switch (hAlign){
        case 1://ALIGN CENTER
            rData->pen.x = rData->minPen.x + ((width - lExtW) / 2); 
        break;  
        case 2://ALIGN RIGHT
            rData->pen.x = rData->minPen.x + (width - lExtW); 
            break;  
        case 3://JUSTIFY
            if (numCodepoints > 1 )
            {   
                //align left and apply letterspacing
                rData->pen.x = rData->minPen.x; 
                if (lExtH <= lineHeightLogical(rData)) {
                    rData->letterSpacing += ((double)(width - lExtW)) /(numCodepoints-1);
                } else {
                    //TODO justify on multi line texts not supported
                }
            }
            else
            {
                //empty text or single character - align center
                rData->pen.x = rData->minPen.x + ((width - lExtW) / 2); 
            }
            break;
        case 0://ALIGN LEFT
        default :
            rData->pen.x = rData->minPen.x; 
            break;

    }
    switch (vAlign){
        case 1://ALIGN MIDDLE
            rData->pen.y = rData->minPen.y + ((height - lExtH) / 2); 
        break;  
        case 2://ALIGN BOTTOM
            rData->pen.y = rData->minPen.y + (height - lExtH); 
            break;  
        case 0://ALIGN TOP
        default :
            rData->pen.y = rData->minPen.y; 
            break;

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
    sft->xScale = font->fontSize;
    sft->yScale = font->fontSize;
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
		        LOG("LineMetrics of %s asc %f, desc %f, gap %f",
                    filename(font->ttfFilePath),
                    font->lineMetrics.ascender,
                    font->lineMetrics.descender,
                    font->lineMetrics.lineGap);
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

static inline int32_t dToI (double d)
{
    return (int32_t)(d );
    // //convert with rounding following the usual math rules
    // if (d < 0){
    //     return (int32_t)(d - 0.5);
    // } else {
    //     return (int32_t)(d + 0.5);
    // }
}

static inline uint32_t utf16Read(const uint16_t* restrict * text) {
    const uint16_t* p = *text;
    uint32_t cp = *p++;
    if (cp == '\0') {
        return '\0';
    } else {
        if (cp >= 0xD800 && cp <= 0xDBFF)
        {
            uint32_t lo = *p++;
            if (lo >= 0xDC00 && lo <= 0xDFFF)
            {
                cp = 0x10000 + (((cp - 0xD800) << 10) | (lo - 0xDC00));
            }
        }

        *text = p;
        return cp;
    }
}
static inline void utf16Skip(const uint16_t* restrict * text) {
    utf16Read(text);
}
static inline uint32_t utf16Peek(const uint16_t* restrict text) {
    return utf16Read(&text);
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

static inline int32_t wholePart(double d) {
    return (int32_t) d;
}
static inline double fractionalPart(double d) {
    return d - wholePart(d);
}

static inline void advancePenBeforeRender(T_ErrorHandler* eh, const SFT* sft, double letterSpacing, uint32_t c1, uint32_t c2, double* penx) {
    if (c1 != 0) {
        SFT_Glyph g1;
        if (sft_lookup(sft, c1, &g1) < 0) {
            ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", c1);
            return;
        }
        SFT_Glyph g2;
        if (sft_lookup(sft, c2, &g2) < 0) {
            ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", c2);
            return;
        }
        SFT_Kerning kerning;
        if (sft_kerning(sft, g1, g2, &kerning) < 0) {
            ERROR(eh, QLS_ERROR_GLIPH_KERNING, "Invalid kerning between codepoints 0x%04X 0x%04X ", c1, c2);
            return;
        }

        *penx += letterSpacing;
        *penx += kerning.xShift;
    }
}
static inline void renderGlyph(T_ErrorHandler* eh, SFT* sft, const T_DPoint* pen, uint32_t color, uint32_t codepoint, T_SurfaceData* surface) {
    sft->xOffset = fractionalPart(pen->x);
    sft->yOffset = fractionalPart(pen->y);

    SFT_Glyph glyph;
    if (sft_lookup(sft, codepoint, &glyph) < 0) {
		ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", codepoint);
        return;
    }

    SFT_GMetrics gmtx;
    if (sft_gmetrics(sft, glyph, &gmtx) < 0) {
        ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X bad glyph metrics", codepoint);
        return;
    }

    SFT_Image img = {
        .width = (gmtx.minWidth + 3) & ~3, //TODO consider aligning to 8?
        .height = gmtx.minHeight
    };
    // VLA!
    uint8_t pixels[img.width * img.height];
    memset(pixels, 0, img.width * img.height);
    img.pixels = pixels;

    if (sft_render(sft, glyph, img) < 0) {
        ERROR(eh, QLS_ERROR_GLIPH_RENDER, "codepoint 0x%04X not rendered", codepoint);
        return;
    }

    const int32_t screenX = wholePart(pen->x) + wholePart(gmtx.leftSideBearing);
    const int32_t screenY = wholePart(pen->y) + gmtx.yOffset;
    copy_rect(screenX, screenY, surface, &img, color);
}
static inline void advancePenAfterRender(T_ErrorHandler* eh, const SFT* sft, double spaceJustification, uint32_t codepoint, double* penx) {
    SFT_Glyph g;
    if (sft_lookup(sft, codepoint, &g) < 0) {
        ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X missing", codepoint);
        return;
    }
    SFT_GMetrics gMetrics;
    if (sft_gmetrics(sft, g, &gMetrics) < 0) {
        ERROR(eh, QLS_ERROR_GLIPH_MISSING, "codepoint 0x%04X bad glyph metrics", codepoint);
        return;
    }

    if (ctypeIsSpace(codepoint)) {
        *penx += spaceJustification;
    }
    *penx += gMetrics.advanceWidth;
}

static inline bool prescanLine_IsLineTooWide(double availableWidth, uint_fast16_t l, double w) {
    return 1 <= l && availableWidth < w;
}
static inline void prescanLine(T_ErrorHandler* eh, SFT sft, const uint16_t* restrict const text, E_QLS_WRAP wrapMode, double availableWidth, double letterSpacing
        , T_Result_PrescanLine* outResult) {

    sft.xOffset = 0;
    
    typedef struct {
        double w, wSpace;
        uint_fast16_t l, lSpace;
        uint32_t lastCodepoint;
    } T_LayoutData;

    T_LayoutData line = {0};
    { // line = ...
        enum { FOR_CHAR_BREAK, FOR_WORD_BREAK };
        T_LayoutData candidates[2] = { line, line };
        
        const uint16_t* reader = text;
        uint32_t codepoint;
        // find line ending, or shortest non-fitting line of at least one codepoint
        while (!(ctypeIsLineEnding(codepoint = utf16Peek(reader)) || prescanLine_IsLineTooWide(availableWidth, line.l, line.w))) {

            candidates[FOR_CHAR_BREAK] = line;
            { // OPT candidates[FOR_WORD_BREAK] = ...
                const bool isWordBreak = line.l == 0 || (ctypeIsSpace(line.lastCodepoint) != ctypeIsSpace(codepoint));
                if (isWordBreak) {
                    candidates[FOR_WORD_BREAK] = line;
                }
            }

            advancePenBeforeRender(eh, &sft, letterSpacing, line.lastCodepoint, codepoint, &line.wSpace);
            if (eh->code != QLS_ERROR_OK) {
                return;
            }
            advancePenAfterRender(eh, &sft, 0, codepoint, &line.wSpace);
            if (eh->code != QLS_ERROR_OK) {
                return;
            }

            line.lastCodepoint = codepoint;
            line.lSpace += 1;
            if (ctypeIsGraphical(codepoint)) {
                line.l = line.lSpace;
                line.w = line.wSpace;
            }
            utf16Skip(&reader);
        }

        const bool wrapchar = wrapMode != QLS_WRAP_WORD;
        const bool wrapword = wrapMode != QLS_WRAP_CHAR;
        if (!prescanLine_IsLineTooWide(availableWidth, line.l, line.w)) {
            // line = line;
        } else if (wrapword && 0 < candidates[FOR_WORD_BREAK].lSpace) {
            line = candidates[FOR_WORD_BREAK];
        } else if (wrapchar && 0 < candidates[FOR_CHAR_BREAK].lSpace) {
            line = candidates[FOR_CHAR_BREAK];
        } else if (wrapchar) {
            // line = line;
        } else /* wrapword */ {
            // include the rest of the last word
            while (ctypeIsGraphical(codepoint = utf16Peek(reader))) {
                advancePenBeforeRender(eh, &sft, letterSpacing, line.lastCodepoint, codepoint, &line.wSpace);
                if (eh->code != QLS_ERROR_OK) {
                    return;
                }
                advancePenAfterRender(eh, &sft, 0, codepoint, &line.wSpace);
                if (eh->code != QLS_ERROR_OK) {
                    return;
                }

                line.lastCodepoint = codepoint;
                line.lSpace += 1;
                line.l = line.lSpace;
                line.w = line.wSpace;
                utf16Skip(&reader);
            }
        }
    }

    // with `line`, *outResult = ...

    const uint16_t* reader = text;
    uint32_t codepoint;
    outResult->len = line.l;
    outResult->width = line.w;
    { // nSpaces = ...; lineEnd = ...
        outResult->nSpaces = 0;
        for (uint_fast16_t i = 0; i < line.l; ++i) {
            if (ctypeIsSpace(codepoint = utf16Read(&reader))) {
                outResult->nSpaces += 1;
            }
        }
        outResult->lineEnd = reader;
    }
    {
        // nextLineStart = ... : Skip any trailing spaces and hard wrap (if present)
        // wasLastLine = ...
        while (ctypeIsSpace(codepoint = utf16Peek(reader))) {
            utf16Skip(&reader);
        }
        outResult->wasLastLine = '\0' == utf16Peek(reader);
        if ('\r' == (codepoint = utf16Peek(reader))) {
            utf16Skip(&reader);
        }
        if ('\n' == (codepoint = utf16Peek(reader))) {
            utf16Skip(&reader);
        }
        outResult->nextLineStart = reader;
    }
}

static inline int32_t renderData_GetWidth(const T_RenderData* r) {
    return r->maxPen.x - r->minPen.x;
}
static inline double getSpareWidth(int32_t boxWidth, double lineWidth) {
    return boxWidth - lineWidth;
}
static inline int32_t renderData_GetHeight(const T_RenderData* r) {
    return r->maxPen.y - r->minPen.y;
}
static inline double getSpareHeight(int32_t boxHeight, double textHeight) {
    return boxHeight - textHeight;
}

static inline void prescanText(T_ErrorHandler* eh, const T_RenderData* r, const uint16_t* restrict const text, T_Result_PrescanText* outResult) {
    SFT sft = r->sft;
    sft.xOffset = 0;
    sft.yOffset = 0;

    uint_fast16_t nLines = 0;
    const uint16_t* reader = text;
    T_Result_PrescanLine lineData = {0};
    do {
        prescanLine(eh, sft, reader, r->wrapMode, renderData_GetWidth(r), r->letterSpacing, &lineData);
        if (eh->code != QLS_ERROR_OK) {
            return;
        }
        nLines += 1;
        reader = lineData.nextLineStart;
    } while (!lineData.wasLastLine);

    outResult->textHeight = nLines * (r->lineMetrics.ascender - r->lineMetrics.descender + r->lineMetrics.lineGap) - r->lineMetrics.lineGap;
}

static inline T_SizeInt layoutAndRender(T_ErrorHandler* eh, const T_RenderData* r, const uint16_t* restrict const text, T_SurfaceData* surface) {
    /**
     * TODO
     * pen.y calculation will be complicated by the fallback-font feature
     * Should we do anything special with text-leading and text-trailing blank lines?
     * Should result.height = 0, when the whole text is blank?
     */

    SFT sft = r->sft;
    T_DPoint pen = {
        .x = r->minPen.x,
        .y = r->minPen.y + r->lineMetrics.ascender
    };
    
    enum { VALIGN_TOP, VALIGN_MIDDLE, VALIGN_BOTTOM };
    switch (r->vAlign) {
        default:
        case VALIGN_TOP: {
            break;
        }
        case VALIGN_MIDDLE: {
            T_Result_PrescanText textData = {0};
            prescanText(eh, r, text, &textData);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            pen.y += getSpareHeight(renderData_GetHeight(r), textData.textHeight) / 2;
            break;
        }
        case VALIGN_BOTTOM: {
            T_Result_PrescanText textData = {0};
            prescanText(eh, r, text, &textData);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }
            pen.y += getSpareHeight(renderData_GetHeight(r), textData.textHeight);
            break;
        }
    }

    uint_fast16_t iLine = 0;
    const uint16_t* reader = text;
    T_Result_PrescanLine lineData = {0};
    double lineWidthMax = 0;
    do {
        prescanLine(eh, r->sft, reader, r->wrapMode, renderData_GetWidth(r), r->letterSpacing, &lineData);
        if (eh->code != QLS_ERROR_OK) {
            return (T_SizeInt) {0, 0};
        }
        lineWidthMax = MAX(lineWidthMax, lineData.width);

        pen.x = r->minPen.x;
        double spaceJustification = 0;
        enum { HALIGN_LEFT, HALIGN_MIDDLE, HALIGN_RIGHT, HALIGN_JUSTIFY }; //TODO single source of truth
        switch (r->hAlign) {
            default:
            case HALIGN_LEFT: {
                break;
            }
            case HALIGN_MIDDLE: {
                pen.x += getSpareWidth(renderData_GetWidth(r), lineData.width) / 2;
                break;
            }
            case HALIGN_RIGHT: {
                pen.x += getSpareWidth(renderData_GetWidth(r), lineData.width);
                break;
            }
            case HALIGN_JUSTIFY: {
                if (0 < lineData.nSpaces && 0 < getSpareWidth(renderData_GetWidth(r), lineData.width)) {
                    spaceJustification = getSpareWidth(renderData_GetWidth(r), lineData.width) / lineData.nSpaces;
                    lineWidthMax = MAX(lineWidthMax, renderData_GetWidth(r));
                }
                break;
            }
        }

        uint32_t lastCodepoint = 0;
        for (uint_fast16_t i = 0; i < lineData.len; ++i) {
            uint32_t codepoint = utf16Peek(reader);
            advancePenBeforeRender(eh, &sft, r->letterSpacing, lastCodepoint, codepoint, &pen.x);
            if (eh->code != QLS_ERROR_OK)  {
                return (T_SizeInt) {0, 0};
            }
            if (surface != NULL) {
                renderGlyph(eh, &sft, &pen, r->color, codepoint, surface);
                if (eh->code != QLS_ERROR_OK) {
                    return (T_SizeInt) {0, 0};
                }
            }
            advancePenAfterRender(eh, &sft, spaceJustification, codepoint, &pen.x);
            if (eh->code != QLS_ERROR_OK) {
                return (T_SizeInt) {0, 0};
            }

            lastCodepoint = codepoint;
            utf16Skip(&reader);
        }

        iLine += 1;
        reader = lineData.nextLineStart;
        pen.y += r->lineMetrics.ascender - r->lineMetrics.descender + r->lineMetrics.lineGap;
    } while (!lineData.wasLastLine);

    return (T_SizeInt) {
        .height = iLine * (r->lineMetrics.ascender - r->lineMetrics.descender + r->lineMetrics.lineGap) - r->lineMetrics.lineGap,
        .width = lineWidthMax
    };
}

int main() {
    puts("Hello World!");
    uint16_t message[256] = {0};
    {
        const unsigned char helloworld[] = "Hello World! ";
        uint16_t* writer = message;
        for (int j = 0; j < 5; ++j) {
            for (int i = 0; helloworld[i] != '\0'; ++i) {
                *writer++ = helloworld[i];
            }
        }
    }

    enum { HEIGHT = 50, WIDTH = 150, STRIDE = ((WIDTH + 3) & ~3)};
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
        .letterSpacing = 1,
        .hAlign = HALIGN_JUSTIFY,
        .vAlign = VALIGN_BOTTOM
    };

    enum { PX_SIZE = 8 };
    r.sft.flags |= SFT_DOWNWARD_Y;
    r.sft.xScale = PX_SIZE;
    r.sft.yScale = PX_SIZE;
    r.sft.font = sft_loadfile("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");

    sft_lmetrics(&r.sft, &r.lineMetrics);

    T_ErrorHandler eh = {0};


    layoutAndRender(&eh, &r, message, &surface);

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

    return eh.code;
}
