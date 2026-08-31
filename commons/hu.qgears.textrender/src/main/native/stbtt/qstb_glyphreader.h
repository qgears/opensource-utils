#ifndef INCLUDED_QST_GLYPHREADER_H
#define INCLUDED_QST_GLYPHREADER_H

#include <stddef.h>
#include "qstb_ctype.h"

struct glyphreader {
    const uint16_t* s;
    const uint16_t* s_unsanitized;
    size_t len, off, end;
    char32_t codepoint;
};

struct glyphreader glyphreader_init(const uint16_t* s_unsanitized);
struct glyphreader glyphreader_read(struct glyphreader gr);
struct glyphreader glyphreader_seek(struct glyphreader gr, size_t off);

#endif
