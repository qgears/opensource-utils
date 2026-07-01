#include "nativeLibLibschrift.h"
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include "schrift.h"
#include <string.h>
#include <fontconfig/fontconfig.h>


#define LOG(...) ;printf(__VA_ARGS__);printf("\n");fflush(stdout)


// Structure to represent surface data
typedef struct {
    uint8_t* data;
    int32_t width;
    int32_t height;
} T_SurfaceData;

typedef struct {
    double x;
    int32_t y;
    int32_t startX;
    int32_t startY;
    int32_t endX;
    int32_t endY;
} T_LayoutData;

static T_SurfaceData* qls_get_surfacedata(uint64_t id);
static inline void qls_render_gliph(T_ErrorHandler* eh,SFT* sft, uint32_t codePoint,T_SurfaceData* surface,T_LayoutData* l_data);
static void qls_load_font(T_ErrorHandler* eh,SFT* sft, T_TrueTypeFont* font);
static void get_font_file(T_ErrorHandler* eh, T_TrueTypeFont* font, char* filePath, uint32_t filePathLength);

uint64_t qls_createSurfaceWithDataPrivate(uint8_t* data, int32_t w, int32_t h)
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
    return (uint64_t)(uintptr_t)surfaceData;
}

T_SizeInt qls_renderTextPrivate(T_ErrorHandler* errorHandler, uint64_t surfaceHandle, T_TrueTypeFont* font, const uint16_t* text, uint32_t textLen,
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
{
    (void)surfaceHandle;
    (void)hAlign;
    (void)vAlign;
    (void)x;
    (void)y;
    (void)width;
    (void)height;
    (void)r;
    (void)g;
    (void)b;
    (void)a;
    (void)clip;
    (void)wrapMode;
    
    T_LayoutData l_data = {0};
    l_data.x = x;
    l_data.y = y;
    T_SurfaceData* surface = qls_get_surfacedata(surfaceHandle);
    if (surface) {
        uint32_t* surfaceData = (uint32_t*)surface->data;
        if (surfaceData) 
        {
            SFT sft = {
		    	.flags  = SFT_DOWNWARD_Y,
            };
            qls_load_font(errorHandler, &sft,font);
            for (int i = 0; i < textLen && (errorHandler->code == QLS_ERROR_OK); i++) {
                //The unicode codepoint
                uint32_t cp = text[i];
                if (cp >= 0xD800 && cp <= 0xDBFF && i + 1 < textLen) {
                    uint32_t lo = text[i + 1];
                    if (lo >= 0xDC00 && lo <= 0xDFFF) {
                        cp = 0x10000 + (((cp - 0xD800) << 10) | (lo - 0xDC00));
                        ++i;
                    }
                }
                qls_render_gliph(errorHandler,&sft, cp,surface,&l_data);
            }
            if (sft.font != NULL){
                sft_freefont(sft.font);
            }

            T_SizeInt result = {100, 50}; // dummy values
            return result;
        } 
        else
        {
            ERROR(errorHandler,QLS_ERROR_SURFACE_DATA_NULL,"Surface data null");
        }
    } else
    {
        ERROR(errorHandler,QLS_ERROR_INVALID_SURFACE,"Invalid surface id");
    }
    
    T_SizeInt result = {0, 0};
    return result;
}

T_SizeInt qls_layoutTextPrivate(T_ErrorHandler* errorHandler, T_TrueTypeFont* font, const uint16_t* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{
    (void)text;
    (void)hAlign;
    (void)vAlign;
    (void)width;
    (void)height;
    (void)wrapMode;
    
    // Stub implementation - to be filled later
    T_SizeInt result = {100, 50}; // dummy values
    return result;
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

static T_SurfaceData* qls_get_surfacedata(uint64_t id) {
    // Cast the handle back to T_SurfaceData pointer
    return (T_SurfaceData*)id;
}


#define MAX(a,b) ((a) > (b) ? (a) : (b))
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
    uint32_t* surfaceData = (uint32_t*)surface->data;
    uint8_t* imageData = ((uint8_t*)img->pixels);
    for (j = min_src_x,dstj = startx; j < max_src_x; j++, dstj++ ) {
        for (i = min_src_y, dsti = starty; i < max_src_y; i++, dsti++ ) {
            int32_t dst_pix = (dsti*surface->width) + dstj;
            int32_t src_pix = (i*img->width) + j;
            uint8_t src_alpha = imageData[src_pix];
            if (src_alpha > 0) {  // Only copy non-transparent pixels
                //TODO proper blending
                surfaceData[dst_pix] = ((color & 0x00FFFFFF) | (src_alpha << 24));
            }
        }
    }
}

// static inline uint8_t blend8(uint8_t src, uint8_t dst, uint8_t alpha)
// {
//     return (uint8_t)((src * alpha + dst * (255 - alpha) + 127) / 255);
// }

static inline void qls_render_gliph(T_ErrorHandler* eh, SFT* sft, uint32_t cp,T_SurfaceData* surface,T_LayoutData* l_data)
{

	SFT_Glyph gid;  //  unsigned long gid;
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
    
    bool render = true;
    int32_t yBaseLine = l_data->y+surface->height;
    if (render){
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
        copy_rect((int32_t)l_data->x ,yBaseLine+mtx.yOffset, surface,&img,0xFFFF0000);
    }
    l_data->x += mtx.advanceWidth;
}

static void qls_load_font(T_ErrorHandler* eh, SFT* sft, T_TrueTypeFont* font) {
    //TODO font cache, load font by name etc...
    sft->xScale = font->fontSize;
    sft->yScale = font->fontSize;
    static char font_path[256];
    get_font_file(eh,font,font_path,sizeof(font_path));

    if (eh->code == QLS_ERROR_OK){
        sft->font = sft_loadfile(font_path);
        if (sft->font == NULL)
        {
            ERROR(eh,QLS_ERROR_FONT_LOAD, "TTF load failed %s" , font->fontFamily);
        }
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