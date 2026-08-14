#include <assert.h>
#include <stdbool.h>
#include <stdio.h>

#include "stb_truetype.h"

#include "qstb_utf8.h"
#include "qstb_line.h"

uint8_t bufFont[1024 * 1024];
uint8_t message[1024];

static void init_message16(void) {
    const char* msg = "Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!\n";
    uint8_t* wSeek = message + 1;
    const char* rSeek = msg;
    while (*rSeek != '\0') {
        *wSeek = *rSeek;
        rSeek += 1;
        wSeek += 2;
    }
    enum {
        SURROGATE_MASK = 0xFC00
        , HIGH_SURROGATE_PREFIX = 0xD800
        , LOW_SURROGATE_PREFIX = 0xDC00
        , VALUE_MASK = 0x03FF
    };
    enum {
        CODEPOINT_OLD_ITALIC_LETTER_EM = 0x01030C
        , CODEPOINT_GRINNING_FACE = 0x01F600
    };
    const int32_t additional_codepoint = CODEPOINT_OLD_ITALIC_LETTER_EM - 0x10000;
    wSeek -= 1;
    wSeek[0] |= HIGH_SURROGATE_PREFIX >> 8;
    wSeek[0] |= (additional_codepoint >> 18) & 0x03;
    wSeek[1] |= (additional_codepoint >> 10) & 0xFF;
    wSeek[2] |= LOW_SURROGATE_PREFIX >> 8;
    wSeek[2] |= (additional_codepoint >> 8) & 0x03;
    wSeek[3] |= (additional_codepoint) & 0xFF;
}

static void init_message8(void) {
    const char* msg = "Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!\n";
    uint8_t* wSeek = message;
    const char* rSeek = msg;
    while (*rSeek != '\0') {
        *wSeek = *rSeek;
        rSeek += 1;
        wSeek += 1;
    }
    enum {
        CODEPOINT_OLD_ITALIC_LETTER_EM = 0x01030C
        , CODEPOINT_GRINNING_FACE = 0x01F600
    };
    wSeek[0] |= 0xF0u | ((CODEPOINT_OLD_ITALIC_LETTER_EM >> 18u));
    wSeek[1] |= 0x80u | ((CODEPOINT_OLD_ITALIC_LETTER_EM >> 12u) & 0x3Fu);
    wSeek[2] |= 0x80u | ((CODEPOINT_OLD_ITALIC_LETTER_EM >> 6u) & 0x3Fu);
    wSeek[3] |= 0x80u | ((CODEPOINT_OLD_ITALIC_LETTER_EM) & 0x3Fu);
}

int main(void) {
    stbtt_fontinfo font;
    {
		FILE* fFont = fopen("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "rb");
		assert(fFont != NULL);
		enum { N_ITEMS = 1000000 };
	    size_t ret = fread(bufFont, 1, 1000000, fFont);
	    assert(ret == N_ITEMS);
	    assert(!ferror(fFont));
	    fclose(fFont);
    }
    stbtt_InitFont(&font, bufFont, 0);

    const float scale = stbtt_ScaleForPixelHeight(&font, 16);
    init_message8();

    struct utf8 utf8;
    utf8 = utf8_init(message);
    // for (utf8 = utf8_init(message); utf8.codepoint != 0; utf8 = utf8_read(utf8)) {
    //     printf("0x%04x %c\n", utf8.codepoint, utf8.codepoint);
    //     struct {
    //         unsigned char* p;
    //         int w, h;
    //     } bmp = {};
    //     bmp.p = stbtt_GetCodepointBitmap(&font, scale, scale, utf8.codepoint, &bmp.w, &bmp.h, NULL, NULL);
    //     for (int y = 0; y < bmp.h; y++) {
    //         for (int x = 0; x < bmp.w; x++) {
    //             putchar(" .:ioVM@"[bmp.p[y * bmp.w + x] >> 5]);
    //         }
    //         putchar('\n');
    //     }
    // }

    enum { WIDTH = 10 };
    struct LineInfo resultLine = line_peek((struct LineInfo){0}, utf8, WIDTH, scale, 0.0, &font, WRAP_WORD);

    for (utf8 = utf8_seek(utf8, 0); utf8.codepoint != 0 && utf8 .off < resultLine.end; utf8 = utf8_read(utf8)) {
        printf("0x%04x %c\n\n", utf8.codepoint, utf8.codepoint);
        struct {
            unsigned char* p;
            int w, h;
        } bmp = {0};
        bmp.p = stbtt_GetCodepointBitmap(&font, scale, scale, utf8.codepoint, &bmp.w, &bmp.h, NULL, NULL);
        for (int y = 0; y < bmp.h; y++) {
            for (int x = 0; x < bmp.w; x++) {
                putchar(" .:ioVM@"[bmp.p[y * bmp.w + x] >> 5]);
            }
            putchar('\n');
        }
        putchar('\n');
    }

    return 0;
}
