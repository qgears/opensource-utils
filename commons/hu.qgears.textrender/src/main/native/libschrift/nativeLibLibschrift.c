#include "nativeLibLibschrift.h"
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include "schrift.h"
#include <string.h>
#include <fontconfig/fontconfig.h>
#include <math.h>

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
} T_RenderData;

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

/*********************************************/
/*** External function implementations     ***/
/*********************************************/

void qls_clearSurfacePrivate(uint64_t id) {
    T_SurfaceData* surface = qls_get_surfacedata(id);
    if (surface->data) {
		int32_t pixelSize = surface->color ? 4 : 1;
		int32_t stride = surface->color ? surface->width : surface->width + 3 & 4;
        memset(surface->data, 0, surface->height * stride * pixelSize);
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
                qls_align(errorHandler,&rData,hAlign,vAlign,text,textLen);
                rData.color = PIX(r) | (PIX(g) << 8) | (PIX(b) << 16) | (PIX(a) << 24);
                qls_layoutAndRender(errorHandler,&rData,surface,text,textLen);

                result.width = dToI(rData.lExtentMax.x-rData.lExtentMin.x);
                result.height = dToI(rData.lExtentMax.y+rData.lExtentMin.y);
            }
            else
            {
                //specified target rectangle is invalid or empty
            }
            if (rData.sft.font != NULL){
                //sft_freefont(rData.sft.font);
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
        if (r.sft.font != NULL){
            //sft_freefont(r.sft.font);
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
    static bool isInited = false;
    static SFT_Font* zaFont;
    if (isInited) {
        sft->font = zaFont;
        return;
    } else {
        isInited = true;
    }
    //TODO font cache, load font by name etc...
    sft->xScale = font->fontSize;
    sft->yScale = font->fontSize;
    sft->flags = SFT_DOWNWARD_Y;
    static char font_path[256];
    get_font_file(eh,font,font_path,sizeof(font_path));

    if (eh->code == QLS_ERROR_OK)
    {
        sft->font = sft_loadfile(font_path);
        zaFont = sft->font;
        if (sft->font == NULL)
        {
            ERROR(eh,QLS_ERROR_FONT_LOAD, "TTF load failed %s" , font->fontFamily);
        }
    }
    
    if (sft_lmetrics(sft,&(r->lineMetrics)) < 0) {
        ERROR(eh,QLS_ERROR_LINE_METRICS, "Failed to init line metrics of font %s" , font->fontFamily);
    } else {
        LOG("LineMetrics asc %f, desc %f, gap %f",r->lineMetrics.ascender, r->lineMetrics.descender, r->lineMetrics.lineGap);
    }

}

static void get_font_file(T_ErrorHandler* eh,T_TrueTypeFont* font, char* filePath, uint32_t filePathLength){
    FcInit();

    FcPattern *pat = FcPatternCreate();

    FcPatternAddString(pat, FC_FAMILY, (FcChar8 *)font->fontFamily);
    FcPatternAddInteger(pat, FC_WEIGHT, font->bold ? FC_WEIGHT_BOLD : FC_WEIGHT_NORMAL);
    FcPatternAddInteger(pat, FC_SLANT, font->italic ? FC_SLANT_ITALIC : FC_SLANT_ROMAN);

    FcConfigSubstitute(NULL, pat, FcMatchPattern);
    FcDefaultSubstitute(pat);

    FcResult result;
    FcPattern *fc_font = FcFontMatch(NULL, pat, &result);

    if (fc_font) {
        char *file;
        int index;

        if (FcPatternGetString(fc_font, FC_FILE, 0, (FcChar8**)&file) == FcResultMatch) {
            uint32_t fLen =(uint32_t) strlen(file);
            if (fLen < filePathLength-1){
                memcpy(filePath,file,fLen);
                filePath[fLen] = '\0';
                LOG("Font file: %s\n", filePath);
            } else {
                ERROR(eh,QLS_ERROR_LONG_FONT_PATH,"File path for font %s too long : %d", font->fontFamily,fLen);
            }
        }
        FcPatternDestroy(fc_font);
    } else {
        ERROR(eh,QLS_ERROR_MISSING_FONT ,"No matching font found %s.",font->fontFamily);
    }
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
