#ifndef INCLUDED_QSTB_UTF8_H
#define INCLUDED_QSTB_UTF8_H

#include <stddef.h>
#include <stdint.h>

#include "qstb_ctype.h"

struct utf8 {
    const uint8_t* s_unsanitized;
    const uint8_t* s;
    size_t len;
    size_t off, end;
    char32_t codepoint;
};

struct utf8 utf8_init(const uint8_t* s);
struct utf8 utf8_read(struct utf8 s);
struct utf8 utf8_seek(struct utf8 s, size_t off);

#endif
