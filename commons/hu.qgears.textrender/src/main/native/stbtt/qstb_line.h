#ifndef INCLUDED_QSTB_LINE_H
#define INCLUDED_QSTB_LINE_H

#include <stddef.h>
#include <stdint.h>

#include "stb_truetype.h"

enum { WRAP_CHAR, WRAP_WORD, WRAP_WORDCHAR };

struct LineInfo {
    size_t off, end;
    uint32_t wSpace, w;
    uint32_t lSpace, l;
};

double line_width(struct LineInfo line, float scale, double letterSpacing);
struct LineInfo line_peek(struct LineInfo line, struct utf8 utf8
                          , int32_t width, float scale, double letterSpacing
                          , const stbtt_fontinfo* font, uint32_t wrapmode);
struct LineInfo line_next(struct LineInfo line, struct utf8 utf8
                          , int32_t width, float scale, double letterSpacing
                          , const stbtt_fontinfo* font, uint32_t wrapmode);

#endif
