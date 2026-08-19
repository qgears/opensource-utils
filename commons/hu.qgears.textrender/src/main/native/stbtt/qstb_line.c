#include <stdint.h>
#include <stdbool.h>
#include <assert.h>

#include "stb_truetype.h"
#include "nativeLibStbtt.h"
#include "qstb_ctype.h"
#include "qstb_glyphreader.h"

#include "qstb_line.h"

enum { WRAP_CHAR, WRAP_WORD, WRAP_WORDCHAR };

struct line line_init(size_t off) {
    return (struct line) {
        .off = off,
        .end = off
    };
}

static bool is_line_too_wide(struct line line, int32_t available_width_scaled, float scale) {
    if (line.l == 0) {
        return false;
    }

    return available_width_scaled < line.w * scale;
}

struct line line_extend(const T_TrueTypeFont* font, struct line line, struct glyphreader gr) {
    line.end = gr.end;

    { //line.wSpace += ...
        int32_t advanceWidth = 0;
        stbtt_GetCodepointHMetrics(&font->stb.font, gr.codepoint, &advanceWidth, NULL);
        line.wSpace += advanceWidth;
        if (line.lastPrintable != 0) {
            line.wSpace += stbtt_GetCodepointKernAdvance(&font->stb.font, line.lastPrintable, gr.codepoint);
        }
    }
    line.lSpace += 1;

    if (is_graph(gr.codepoint)) {
        line.l = line.lSpace;
        line.w = line.wSpace;
    }

    line.lastPrintable = gr.codepoint;
    return line;
}

static struct line line_advance(struct glyphreader gr) {
    if (gr.codepoint == '\r') {
        gr = glyphreader_read(gr);
    }
    if (gr.codepoint == '\n') {
        gr = glyphreader_read(gr);
    }
    return line_init(gr.off);
}

static bool is_word_boundary(char32_t c1, char32_t c2) {
    if (c1 == 0) {
        return false;
    }

    return is_space(c1) != is_space(c2);
}

struct line line_peek(const T_TrueTypeFont* font, struct glyphreader gr, int32_t available_width_scaled, float scale, uint32_t wrapmode) {
    const bool wrapchar = wrapmode != WRAP_WORD;
    const bool wrapword = wrapmode != WRAP_CHAR;

    struct line line = line_init(gr.off);

    char32_t last_printable = 0;
    enum { FOR_CHAR_BREAK, FOR_WORD_BREAK };
    struct line candidates[2] = { line, line };
    while (!(is_line_too_wide(line, available_width_scaled, scale) || is_line_ending(gr.codepoint))) {
        candidates[FOR_CHAR_BREAK] = line;
        if (is_word_boundary(last_printable, gr.codepoint)) {
            candidates[FOR_WORD_BREAK] = line;
        }

        line = line_extend(font, line, gr);

        last_printable = gr.codepoint;
        gr = glyphreader_read(gr);
    }
    assert(   is_line_too_wide(line, available_width_scaled, scale) || is_line_ending(gr.codepoint));

    if (!is_line_too_wide(line, available_width_scaled, scale)) {
        //line = line;
    } else if (wrapword && candidates[FOR_WORD_BREAK].lSpace != 0) {
        line = candidates[FOR_WORD_BREAK];
    } else if (wrapchar && candidates[FOR_CHAR_BREAK].lSpace != 0) {
        line = candidates[FOR_CHAR_BREAK];
    } else if (wrapchar) {
        //line = line;
    } else /*wrapword*/ {
        while (!(is_line_ending(gr.codepoint) || is_space(gr.codepoint))) {
            line = line_extend(font, line, gr);
            last_printable = gr.codepoint;
            gr = glyphreader_read(gr);
        }
    }

    gr = glyphreader_seek(gr, gr.codepoint);
    while (is_space(gr.codepoint)) {
        line = line_extend(font, line, gr);
        last_printable = gr.codepoint;
        gr = glyphreader_read(gr);
    }

    return line;
}

struct line line_next(const T_TrueTypeFont* font, struct glyphreader gr, int32_t available_width_scaled, float scale, uint32_t wrapmode) {
    struct line line = line_advance(gr);
    gr = glyphreader_seek(gr, line.end);
    return line_peek(font, gr, available_width_scaled, scale, wrapmode);
}
