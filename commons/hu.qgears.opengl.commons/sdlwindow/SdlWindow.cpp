#include "hu_qgears_sdlwindow_SdlWindowNative.h"
#define CLASS Java_hu_qgears_sdlwindow_SdlWindowNative_
#include "jniutil.h"

#include <SDL2/SDL.h>
#include <SDL2/SDL_render.h>

#include <iostream>


using namespace std;

static SDL_Window* window;
static SDL_Surface *screen; // even with SDL2, we can still bring ancient code back
static bool firstFrame = true;
static bool running = true;
static int texWidth;
static int texHeight;


METHODPREFIX(CLASS, void, openWindow)(ST_ARGS, jint w, jint h, jstring windowName)
{
    texWidth = w;
    texHeight = h;
    SDL_SetHintWithPriority(SDL_HINT_RENDER_VSYNC, "1", SDL_HINT_OVERRIDE);


	const char* str = env->GetStringUTFChars(windowName,0);
    window = SDL_CreateWindow
        (
        str,
        SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED,
        w, h,
//        SDL_WINDOW_SHOWN - only show the window after the first frame was already rendered
        SDL_WINDOW_HIDDEN
        );
	env->ReleaseStringUTFChars(windowName,str);
	// instead of creating a renderer, we can draw directly to the screen
    screen = SDL_GetWindowSurface(window);
}
METHODPREFIX(CLASS, void, init)(ST_ARGS)
{
    SDL_Init( SDL_INIT_EVERYTHING );
    atexit( SDL_Quit );
}

METHODPREFIX(CLASS, jboolean, processEvents)(ST_ARGS)
{
        SDL_Event event;
        while( SDL_PollEvent( &event ) )
        {
            if( ( SDL_QUIT == event.type ) ||
                ( SDL_KEYDOWN == event.type && SDL_SCANCODE_ESCAPE == event.key.keysym.scancode ) )
            {
                running = false;
                break;
            }
        }
        return running;
}
METHODPREFIX(CLASS, void, drawExample)(ST_ARGS, jobject textureBuffer)
{
    uint8_t * data = (uint8_t *) env->GetDirectBufferAddress(textureBuffer);
    uint32_t capacity = env->GetDirectBufferCapacity(textureBuffer);

	if(capacity<texWidth*texHeight*4)
	{
		cerr << "Texture buffer size too small: " << capacity << " required min: " << (texWidth*texHeight*4);
		return;
	}
        // splat down some random pixels
        for( unsigned int i = 0; i < 1000; i++ )
        {
            const unsigned int x = rand() % texWidth;
            const unsigned int y = rand() % texHeight;

            const unsigned int offset = ( texWidth * 4 * y ) + x * 4;
            data[ offset + 0 ] = rand() % 256;        // b
            data[ offset + 1 ] = rand() % 256;        // g
            data[ offset + 2 ] = rand() % 256;        // r
            data[ offset + 3 ] = SDL_ALPHA_OPAQUE;    // a
        }
}
METHODPREFIX(CLASS, void, updateFrame)(ST_ARGS, jobject textureBuffer)
{
    uint8_t * data = (uint8_t *) env->GetDirectBufferAddress(textureBuffer);
    uint32_t capacity = env->GetDirectBufferCapacity(textureBuffer);

	SDL_Surface *surf =	SDL_CreateRGBSurfaceWithFormatFrom(data, texWidth, texHeight, 32, texWidth*4, SDL_PIXELFORMAT_BGRA8888);
    SDL_BlitSurface(surf, NULL, screen, NULL); // blit it to the screen
    SDL_FreeSurface(surf);
    SDL_UpdateWindowSurface(window);
    if(firstFrame)
    {
     	SDL_ShowWindow(window); // Show the window when the first content is already rendered.
    }
    firstFrame = false;
}

METHODPREFIX(CLASS, void, closeWindow)(ST_ARGS)
{
    SDL_DestroyWindow( window );
    SDL_Quit();
}