#include "hu_qgears_sdlwindow_SdlWindowNative.h"
#define CLASS Java_hu_qgears_sdlwindow_SdlWindowNative_
#include "jniutil.h"

#include <SDL2/SDL.h>
#include <SDL2/SDL_render.h>

#include <iostream>


using namespace std;

static SDL_Window* window;
static SDL_Renderer* renderer;
static SDL_Texture* texture;
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

    renderer = SDL_CreateRenderer
        (
        window,
        -1,
        SDL_RENDERER_SOFTWARE|SDL_RENDERER_PRESENTVSYNC
        );
    if(renderer==NULL)
    {
    }
        
    SDL_RendererInfo info;
    SDL_GetRendererInfo( renderer, &info );
    cout << "Renderer name: " << info.name << endl;
    cout << "Texture formats: " << endl;
    if(info.num_texture_formats<0)
    {
     info.num_texture_formats=0;
    }
    if(info.num_texture_formats>100)
    {
     info.num_texture_formats=100;
    }
    for( Uint32 i = 0; i < info.num_texture_formats; i++ )
    {
        cout << SDL_GetPixelFormatName( info.texture_formats[i] ) << endl;
    }

    texture = SDL_CreateTexture
        (
        renderer,
        SDL_PIXELFORMAT_ARGB8888,
        SDL_TEXTUREACCESS_STREAMING,
        texWidth, texHeight
        );
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

	if(capacity<texWidth*texHeight*4)
	{
		cerr << "Texture buffer size too small: " << capacity << " required min: " << (texWidth*texHeight*4);
		return;
	}

//      SDL_SetRenderDrawColor( renderer, 0, 0, 0, SDL_ALPHA_OPAQUE );
//      SDL_RenderClear( renderer );

        SDL_UpdateTexture
            (
            texture,
            NULL,
            data,
            texWidth * 4
            );

        SDL_RenderCopy( renderer, texture, NULL, NULL );
        SDL_RenderPresent( renderer );
        if(firstFrame)
        {
        	SDL_ShowWindow(window); // Show the window when the first content is already rendered.
        }
    firstFrame = false;
}

METHODPREFIX(CLASS, void, closeWindow)(ST_ARGS)
{
    SDL_DestroyRenderer( renderer );
    SDL_DestroyWindow( window );
    SDL_Quit();
}