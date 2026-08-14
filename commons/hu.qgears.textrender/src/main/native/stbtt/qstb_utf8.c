#include <stddef.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>

#include "qstb_ctype.h"

// typedef uint32_t char32_t;

struct utf8 {
    const uint8_t* s_unsanitized;
    const uint8_t* s;
    size_t len;
    size_t off, end;
    char32_t codepoint;
};

struct result_utf8d_read {
    int32_t codepoint;
    size_t end;
};

static struct result_utf8d_read utf8d_read(const uint8_t* s, const size_t off) {

    /*
     * According to docs, java gives us big-endian UTF-8
     *
     * Java encodes string-intermediate null bytes in a non-standard way
     * Java encode 4 byte wide UTF-8 chars in a non-standard way
     *      Here, these are read as a series of U+FFFD (Unicode replacement character)
     */
    char32_t raw = 0;
    {
        size_t j = off;
        for (uint_fast8_t i = 0; i < 4; ++i) {
            raw <<= 8;
            raw |= s[j];
            if (s[j] != '\0') {
                j += 1;
            }
        }
    }

    if (raw == 0u) {
        return (struct result_utf8d_read) {
            .codepoint = '\0',
            .end = off
        };
    }

    const char32_t masks[4] = {
        0x80000000u,
        0xE0C00000u,
        0xF0C0C000u,
        0xF8C0C0C0u
    };
    const char32_t prefixes[4] = {
        0x0u,
        0xC0800000u,
        0xE0808000u,
        0xF0808080u
    };
    const char32_t mins[4] = {
        0x0u,
        0x80u << (2u + 16u) | prefixes[1],
        0x800u << (2u + 8u) | prefixes[2],
        0x010000u << (4u)   | prefixes[3]
    };

    uint_fast8_t k;
    for (k = 0; !(4 <= k || (raw & masks[k]) == prefixes[k]); ++k);
    assert(       4 == k || (raw & masks[k]) == prefixes[k]);

    if (k < 4 && mins[k] <= raw) {
        char32_t codepoint = 0;
        raw &= ~masks[k];
        codepoint |= (raw & 0x3Fu);
        codepoint |= (raw & 0x3F00u) >> 2u;
        codepoint |= (raw & 0x3F0000u) >> 4u;
        codepoint |= (raw & 0x7F000000u) >> 6u;
        if (k != 3) {
            assert(  0u < (3 - k) * 6u);
            codepoint >>= (3 - k) * 6u;
        }

        enum {
            UNICODE_MAX = 0x10FFFF
            , UTF16_SURROGATE_MIN = 0xD800
            , UTF16_SURROGATE_MAX = 0xDFFF
        };
        if (codepoint < UTF16_SURROGATE_MIN
            || (UTF16_SURROGATE_MAX < codepoint && codepoint <= UNICODE_MAX))
        {
            return (struct result_utf8d_read) {
                .codepoint = codepoint,
                .end = off + 1 + k
            };
        }
    }

    return (struct result_utf8d_read) {
        .codepoint = 0xFFFDu, //Unicode replacement character aka. rhombus with question mark
        .end = off + 1
    };
}

static struct utf8 utf8_peek(struct utf8 s) {
    struct result_utf8d_read r = utf8d_read(s.s, s.off);
    s.codepoint = r.codepoint;
    s.end = r.end;
    return s;
}

struct utf8 utf8_init(const uint8_t* s) {
    if (s == NULL) {
        return (struct utf8) {
            .s = (uint8_t*)"\0\0",
            .s_unsanitized = NULL,
            .len = 0,
            .off = 0,
            .end = 0
        };
    }

    struct utf8 result = {
        .s = s,
        .s_unsanitized = s,
        .len = strlen(s),
    };
    result = utf8_peek(result);
    return result;
}

struct utf8 utf8_read(struct utf8 s) {
    struct result_utf8d_read r = utf8d_read(s.s, s.end);
    s.codepoint = r.codepoint;
    s.off = s.end;
    s.end = r.end;
    return s;
}

static size_t clamp(size_t a, size_t b, size_t c) {
    if (!(a < b)) {
        return a;
    }
    if (!(b < c)) {
        return c;
    }
    assert(a < b && b < c);
    return b;
}

struct utf8 utf8_seek(struct utf8 s, size_t off) {
    assert(0 <= off && off <= s.len);
    s.off = clamp(0, off, s.len);
    return utf8_peek(s);
}

struct utf8 utf8_ignore(struct utf8 s) {
    struct utf8 u2 = utf8_read(s);
    while (is_ignore(u2.codepoint)) {
        u2 = utf8_read(u2);
    }
    s.end = u2.off;
    return s;
}