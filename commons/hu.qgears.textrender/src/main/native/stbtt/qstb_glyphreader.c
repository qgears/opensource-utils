#include <stdint.h>
#include <stdbool.h>
#include <assert.h>

#include "qstb_ctype.h"
#include "qstb_glyphreader.h"

#define ERROR(...) //TODO

struct data_read_result {
    char32_t codepoint;
    size_t end;
};

/**
 *
 * @param s
 * @param off Measured in 2 byte units
 * @return
 */
static struct data_read_result glyphreader_data_read(const uint16_t* const s, const size_t off) {
    size_t end = off;

    char32_t raw = 0;
    raw |= s[off];
    if (raw != 0) {
        end += 1;
    }

    enum { SURROGATE_RANGE_START = 0xD800, SURROGATE_RANGE_END = 0xDFFF };
    if (raw < SURROGATE_RANGE_START || SURROGATE_RANGE_END < raw) {
        return (struct data_read_result) {
            .codepoint = raw,
            .end = end
        };
    }

    raw <<= 16u;
    raw |= s[off + 1];
    if ((raw & 0xFFFFu) != 0) {
        end += 1;
    }

    const bool is_prefix_ok = (raw & 0xDC00DC00u) == 0xD800DC00u;
    if (is_prefix_ok) {
        char32_t codepoint = 0;
        codepoint |= raw & 0x03FFu;
        codepoint |= (raw >> 16u & 0x03FFu) << 10u;
        codepoint += 0x10000u;
        enum { LAST_VALID_CODEPOINT = 0x10FFFF };
        const bool is_range_ok = codepoint <= LAST_VALID_CODEPOINT;
        if (is_range_ok) {
            return (struct data_read_result) {
                .codepoint = codepoint,
                .end = end
            };
        }
    }

    enum { UNICODE_REPLACEMENT_CHARACTER = 0xFFFD };
    return (struct data_read_result) {
        .codepoint = UNICODE_REPLACEMENT_CHARACTER,
        .end = off + 1
    };
}

static struct glyphreader glyphreader_peek_simple(struct glyphreader gr) {
    struct data_read_result drr = glyphreader_data_read(gr.s, gr.off);
    gr.codepoint = drr.codepoint;
    gr.end = drr.end;
    return gr;
}

static struct glyphreader glyphreader_read_simple(struct glyphreader gr) {
    gr.off = gr.end;
    return glyphreader_peek_simple(gr);
}

static struct glyphreader glyphreader_peek(struct glyphreader gr) {
    const size_t off = gr.off;
    gr = glyphreader_peek_simple(gr);
    while (is_ignore(gr.codepoint)) {
        gr = glyphreader_read_simple(gr);
    }
    const char32_t codepoint = gr.codepoint;
    while (is_ignore(gr.codepoint)) {
        gr = glyphreader_read_simple(gr);
    }
    gr = glyphreader_read_simple(gr);
    const size_t end = gr.off;

    gr.off = off;
    gr.codepoint = codepoint;
    gr.end = end;
    return gr;
}

struct glyphreader glyphreader_init(const uint16_t* s_unsanitized) {
    if (s_unsanitized == NULL) {
        return (struct glyphreader) {
            .s = (const uint16_t*) "\0\0",
            .s_unsanitized = s_unsanitized,
        };
    }

    struct glyphreader result = {
        .s = s_unsanitized,
        .s_unsanitized = s_unsanitized
    };
    for (; result.s[result.len] != 0; ++result.len);
    return glyphreader_peek(result);
}

struct glyphreader glyphreader_read(struct glyphreader gr) {
    gr.off = gr.end;
    return glyphreader_peek(gr);
}

struct glyphreader glyphreader_seek(struct glyphreader gr, size_t off) {
    {
        //let 0 <= off <= gr.len
        if (off < 0) {
            off = 0;
        }
        if (gr.len < off) {
            off = gr.len;
        }
    }

    gr.off = off;
    return glyphreader_peek(gr);
}
