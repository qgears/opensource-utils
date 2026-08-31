#include <stdio.h>
#include <stdint.h>

#include "nativeLibStbtt.h"
#include "qstb_glyphreader.h"

int main() {
    printf("Hello World!\n");

    enum { WCANVAS = 200, HCANVAS = 50 };
    // static uint32_t canvas[HCANVAS * WCANVAS] = {0};
    // uint64_t surfaceHandle = qstb_createSurfaceWithDataPrivate((uint8_t*) canvas, WCANVAS, HCANVAS, ENICO_BGRA);
    static uint8_t canvas[HCANVAS * WCANVAS] = {0};
    uint64_t surfaceHandle = qstb_createSurfaceWithDataPrivate((uint8_t*) canvas, WCANVAS, HCANVAS, ENICO_ALPHA);

    T_TrueTypeFont font = {0};
    font.fontFamily = "DejaVu Sans";
    font.fontSize = 16;
    enum { WRAP_CHAR, WRAP_WORD, WRAP_WORDCHAR };
    // T_SizeInt asdf = qstb_layoutTextPrivate(&font, (uint16_t*)"H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0\0\0", 0, 0, 300, 100, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0\0\0", 0, 0, 0, 0, 300, 100, 1, 1, 1, 1, true, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0 \0H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0\0\0", 0, 0, 0, 0, 300, 100, 1, 1, 1, 1, false, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)" \0 \0W\0\0\0", 0, 0, 0, 0, 300, 100, 1, 1, 1, 1, true, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0\0\0", 0, 0, 0, 0, WCANVAS, HCANVAS, 1, 1, 1, 1, true, WRAP_WORD);
    // T_SizeInt asdf = qstb_layoutTextPrivate(&font, (uint16_t*)"H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0 \0H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0\0\0", 0, 0, 300, 100, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0e\0l\0l\0o\0\0\0", 0, 0, 0, 0, WCANVAS, HCANVAS, 1, 1, 1, 1, false, WRAP_WORDCHAR);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0e\0l\0l\0o\0\0\0", 0, 0, 0, 0, WCANVAS, HCANVAS, 1, 1, 1, 1, false, WRAP_WORD);
    // T_SizeInt asdf = qstb_layoutTextPrivate(&font, (uint16_t*)"H\0e\0l\0l\0o\0\0\0", 0, 0, WCANVAS, HCANVAS, WRAP_WORDCHAR);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0 \0H\0e\0l\0l\0o\0 \0W\0o\0r\0l\0d\0!\0\0\0", 1, 2, 0, 0, WCANVAS, HCANVAS, 1, 1, 1, 1, false, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"m\0u\0l\0t\0i\0w\0i\0n\0d\0o\0w\0.\0\0\0", 0, 0, 0, 0, WCANVAS, HCANVAS, 1,1,1,1, false, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"H\0\0\0", 0, 0, 0, 0, WCANVAS, HCANVAS, 1,1,1,1, false, WRAP_WORD);
    // T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)"T\0e\0s\0t\0\0", 0,0, 0,0, WCANVAS,HCANVAS, 1,1,1,1, false, WRAP_WORD);
    T_SizeInt asdf = qstb_renderTextPrivate(surfaceHandle, &font, (uint16_t*)".\0e\0.\0s\0.\0e\0s\0.\0T\0e\0s\0.\0.\0\0\0", 0,0, 0,0, WCANVAS,HCANVAS, 1,1,1,1, false, WRAP_WORD);

    printf("%d %d\n", asdf.height, asdf.width);

    for (int y = 0; y < HCANVAS; ++y) {
        for (int x = 0; x < WCANVAS; ++x) {
            putchar(" .:ioVM@"[(*(uint8_t*)(canvas + (y * WCANVAS + x))) >> 5]);
        }
        putchar('\n');
    }
    return 0;
}
