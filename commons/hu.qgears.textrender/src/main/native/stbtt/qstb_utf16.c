#include <stddef.h>
#include <stdint.h>
#include <assert.h>

//alternative name: codepoint stream
struct utf16 {
    const uint8_t* s_unsanitized;
    const uint8_t* s;
    size_t len2;
    size_t off2, end2;
    int32_t codepoint;
};

struct result_utf16d_read {
    int32_t codepoint;
    size_t end2;
};

static struct result_utf16d_read utf16d_read(const uint8_t* s, size_t off2) {
    int16_t data[2] = {};
    size_t ends[2] = {};
    size_t i2 = off2;
    data[0] |= s[i2 * 2];
    data[0] <<= 8;
    data[0] |= s[i2 * 2 + 1];
    if (data[0] != 0) {
        i2 += 1;
    }
    ends[0] = i2;
    data[1] |= s[i2 * 2];
    data[1] <<= 8;
    data[1] |= s[i2 * 2 + 1];
    if (data[1] != 0) {
        i2 += 1;
    }
    ends[1] = i2;

    // 16 bit wide?
    if ((data[0] & 0x8000) == 0) {
        return (struct result_utf16d_read) {
            .codepoint = data[0],
            .end2 = ends[0]
        };
    }

    // 32 bit wide?
    enum {
        SURROGATE_MASK = 0xFC00
        , HIGH_SURROGATE_PREFIX = 0xD800
        , LOW_SURROGATE_PREFIX = 0xDC00
        , VALUE_MASK = 0x03FF
    };
    if ((data[0] & SURROGATE_MASK) == HIGH_SURROGATE_PREFIX && (data[1] & SURROGATE_MASK) == LOW_SURROGATE_PREFIX) {
        int32_t c = 0;
        c |= data[0] & VALUE_MASK;
        c <<= 10;
        c |= data[1] & VALUE_MASK;
        c += 0x010000;
        if (0 <= c && c <= 0x10FFFF) {
            return (struct result_utf16d_read) {
                .codepoint = c,
                .end2 = ends[1]
            };
        }
    }

    // invalid?
    enum { UNICODE_REPLACEMENT_CHARACTER = 0xFFFD }; //aka. rhombus with questionmark
    return (struct result_utf16d_read) {
        .codepoint = UNICODE_REPLACEMENT_CHARACTER,
        .end2 = ends[0]
    };
}

static size_t utf16d_strlen(const uint8_t* s) {
    size_t off2 = 0;
    while (!(s[off2 * 2] == 0x00u && s[off2 * 2 + 1] == 0x00u)) {
        off2 += 1;
    }
    return off2;
}

static struct utf16 utf16_peek(struct utf16 s) {
    struct result_utf16d_read r = utf16d_read(s.s, s.off2);
    s.codepoint = r.codepoint;
    s.end2 = r.end2;
    return s;
}

struct utf16 utf16_init(const uint8_t* s) {
    if (s == NULL) {
        return (struct utf16) {
            .s = (uint8_t*)"\0\0",
            .s_unsanitized = NULL,
            .len2 = 0,
            .off2 = 0,
            .end2 = 0
        };
    }

    struct utf16 result = {
        .s = s,
        .s_unsanitized = s,
        .len2 = utf16d_strlen(s),
    };
    result = utf16_peek(result);
    return result;
}

struct utf16 utf16_read(struct utf16 s) {
    struct result_utf16d_read r = utf16d_read(s.s, s.end2);
    s.codepoint = r.codepoint;
    s.off2 = s.end2;
    s.end2 = r.end2;
    return s;
}

struct utf16 utf16_seek(struct utf16 s, size_t off2) {
    assert(0 <= off2 && off2 <= s.len2);
    s.off2 = (off2 < s.len2) ? off2 : s.len2;
    s.off2 = (s.off2 < 0) ? 0 : s.off2;
    return utf16_peek(s);
}