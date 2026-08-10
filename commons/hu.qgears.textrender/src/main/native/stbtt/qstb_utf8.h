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

/**
 * Gives a usable struct utf8 (with s and len set). Reads the first codepoint.
 *
 * @param s will be assigned to s_sunsanitized
 * @return
 */
struct utf8 utf8_init(const uint8_t* s);
/**
 * Reads the codepoint at s.end (updating s.off and s.end)
 *
 * @param s inited
 * @return
 */
struct utf8 utf8_read(struct utf8 s);

/**
 * Sets s.off and reads the codepoint at s.off
 *
 * @param s inited
 * @param off
 * @return
 */
struct utf8 utf8_seek(struct utf8 s, size_t off);

/**
 * Sets s.end to include codepoints based on is_ignore(...)
 *
 * @param s inited
 * @return
 */
struct utf8 utf8_include_ignore_chars(struct utf8 s);

#endif
