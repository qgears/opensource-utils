#ifndef INCLUDED_QSTB_UTF16_H
#define INCLUDED_QSTB_UTF16_H

#include <stdint.h>

struct utf16 {
    const uint8_t* s_unsanitized;
    const uint8_t* s;
    size_t len2;
    size_t off2, end2;
    int32_t codepoint;
};

struct utf16 utf16_init(const uint8_t* s);
struct utf16 utf16_read(struct utf16 s);
struct utf16 utf16_seek(struct utf16 s, size_t off2);

#endif
