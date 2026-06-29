#include "nativeLibStbtt.h"
#include <stddef.h>
#include <stdlib.h>

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

T_SizeInt qstb_renderTextPrivate(uint64_t surfaceHandle, const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t x, int32_t y, int32_t width, int32_t height,
                            float r, float g, float b, float a, bool clip, uint32_t wrapMode)
{
    T_SurfaceData* surface = qstb_get_surfacedata(surfaceHandle);
    if (surface) {
        uint32_t* surfaceData = (uint32_t*)surface->data;
        if (surfaceData) 
        {
            // Dummy implementation - draw some "random" lines
            uint32_t i = 0;
            uint32_t j = 0;
            for (j = x; j < width; j++ ) {
                surfaceData[i*width + j] = 0xFF0000FF;//RED
                i = ((i+1) % height);
            }
        }
        
        T_SizeInt result = {100, 50}; // dummy values
        return result;
    }
}

T_SizeInt qstb_layoutTextPrivate(const char* fontFamily, const char* text, 
                            uint32_t hAlign, uint32_t vAlign, int32_t width, int32_t height, uint32_t wrapMode)
{
    // Stub implementation - to be filled later
    T_SizeInt result = {100, 50}; // dummy values
    return result;
}

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
