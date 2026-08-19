#ifndef INCLUDED_QSTB_LINE_H
#define INCLUDED_QSTB_LINE_H

#include <stddef.h>

#include "nativeLibStbtt.h"

struct line {
    size_t off, end;
    int32_t w, wSpace, l, lSpace;
    char32_t lastPrintable;
};

struct line line_init(size_t off);
struct line line_extend(const T_TrueTypeFont* font, struct line line, struct glyphreader gr);
struct line line_peek(const T_TrueTypeFont* font, struct glyphreader gr, int32_t available_width_scaled, float scale, uint32_t wrapmode);
struct line line_next(const T_TrueTypeFont* font, struct glyphreader gr, int32_t available_width_scaled, float scale, uint32_t wrapmode);

#endif
