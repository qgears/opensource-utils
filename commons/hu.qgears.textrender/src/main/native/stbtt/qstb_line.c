#include <assert.h>
#include <stddef.h>
#include <stdint.h>

#include "stb_truetype.h"
#include "qstb_utf8.h"
#include "qstb_ctype.h"

enum { WRAP_CHAR, WRAP_WORD, WRAP_WORDCHAR };

struct LineInfo {
    size_t off, end;
    uint32_t wSpace, w;
    uint32_t lSpace, l;
};

static struct LineInfo line_extend(struct LineInfo line, const stbtt_fontinfo* font, char32_t last_printable, struct utf8 utf8) {
    assert(font != NULL);
    //for kerning
    assert(last_printable == '\0' || is_print(last_printable));
    assert(is_print(utf8.codepoint));

    int32_t width = 0, kern = 0;
    stbtt_GetCodepointHMetrics(font, utf8.codepoint, &width, NULL);
    if (last_printable != '\0') {
        kern = stbtt_GetCodepointKernAdvance(font, last_printable, utf8.codepoint);
    }

    line.lSpace += 1;
    line.wSpace += width + kern;
    line.end = utf8.end;
    if (is_graph(utf8.codepoint)) {
        line.w = line.wSpace;
        line.l = line.lSpace;
    }

    return line;
}

static struct LineInfo line_advance(struct LineInfo line, struct utf8 utf8) {
    utf8 = utf8_seek(utf8, line.end);
    if (utf8.codepoint == '\r') {
        utf8 = utf8_read(utf8);
    }
    if (utf8.codepoint == '\n') {
        utf8 = utf8_read(utf8);
    }
    return (struct LineInfo) {
        .off = utf8.off,
        .end = utf8.off
    };
}

static bool is_word_boundary(char32_t c1, char32_t c2) {
    assert(is_print(c1) || c1 == '\0');
    assert(is_print(c2));

    return c1 == '\0' || (is_graph(c1) != is_graph(c2));
}

double line_width(struct LineInfo line, float scale, double letterSpacing) {
    //TODO shared concerns with layout and render
    //TODO negative letter spacing -> a thin character might decrease the logical width, while leaving the ink width untouched
    int32_t nLetterSpacing = (0 < line.l) ? line.l - 1 : 0;
    return line.w * scale + nLetterSpacing * letterSpacing;
}

static bool is_line_too_long(struct LineInfo line, int32_t width, float scale, double letterSpacing) {
    if (line.l <= 0) {
        return false;
    }
    return width < line_width(line, scale, letterSpacing);
}

/**
 * Finds the (soft) line at line.off
 * Does not include \r, \n, \0
 * Includes trailing is_ignore codepoints
 *
 * @param line
 * @param utf8 let the length be initialized
 * @param width
 * @param scale
 * @param letterSpacing
 * @param font
 * @param wrapmode
 * @return
 */
struct LineInfo line_peek(struct LineInfo line, struct utf8 utf8
                          , int32_t width, float scale, double letterSpacing
                          , const stbtt_fontinfo* font, uint32_t wrapmode)
{
    assert(0.0f < scale);

    line = (struct LineInfo) {
        .off = line.off,
        .end = line.off
    };
    utf8 = utf8_seek(utf8, line.end);
    { //include ignore chars in the empty string
        while (is_ignore(utf8.codepoint)) {
            utf8 = utf8_read(utf8);
        }
        line.end = utf8.off;
    }

    const bool wrapchar = wrapmode == WRAP_CHAR || wrapmode == WRAP_WORDCHAR;
    const bool wrapword = wrapmode == WRAP_WORD || wrapmode == WRAP_WORDCHAR;

    char32_t last_printable = '\0';
    enum { FOR_CHAR_BREAK, FOR_WORD_BREAK };
    struct LineInfo candidates[2] = { line, line };

    while (!(is_line_too_long(line, width, scale, letterSpacing) || is_line_ending(utf8.codepoint))) {
        assert(is_print(utf8.codepoint));
        candidates[FOR_CHAR_BREAK] = line;
        if (is_word_boundary(last_printable, utf8.codepoint)) {
            candidates[FOR_WORD_BREAK] = line;
        }

        line = line_extend(line, font, last_printable, utf8);

        last_printable = utf8.codepoint;
        utf8 = utf8_read(utf8);
        while (is_ignore(utf8.codepoint)) {
            utf8 = utf8_read(utf8);
        }
        line.end = utf8.off;
    }
    assert(is_print(utf8.codepoint) || is_line_ending(utf8.codepoint));
    assert(is_line_too_long(line, width, scale, letterSpacing) || is_line_ending(utf8.codepoint));

    if (!is_line_too_long(line, width, scale, letterSpacing)) {
        assert(is_line_ending(utf8.codepoint));
        //line = line;
    } else if (wrapword && 0 < candidates[FOR_WORD_BREAK].lSpace) {
        line = candidates[FOR_WORD_BREAK];
    } else if (wrapchar && 0 < candidates[FOR_CHAR_BREAK].lSpace) {
        line = candidates[FOR_CHAR_BREAK];
    } else if (wrapchar) {
        //line = line;
    } else /*wrap word*/ {
        // utf8 = utf8_seek(utf8, line.end);
        while (!(is_line_ending(utf8.codepoint) || is_space(utf8.codepoint))) {
            assert(is_graph(utf8.codepoint));
            line = line_extend(line, font, last_printable, utf8);

            last_printable = utf8.codepoint;
            utf8 = utf8_read(utf8);
            while (is_ignore(utf8.codepoint)) {
                utf8 = utf8_read(utf8);
            }
            line.end = utf8.off;
        }
        assert(is_line_ending(utf8.codepoint) || is_space(utf8.codepoint));
    }

    utf8 = utf8_seek(utf8, line.end);
    while (is_space(utf8.codepoint) || is_ignore(utf8.codepoint)) {
        utf8 = utf8_read(utf8);
    }
    line.end = utf8.off;

    return line;
}

/**
 * Finds the next (soft) line, after the end of the given one.
 * Does not include \r, \n, \0
 * Includes trailing is_ignore codepoints
 *
 * @param line
 * @param utf8 inited
 * @param width
 * @param scale
 * @param letterSpacing
 * @param font
 * @param wrapmode
 * @return
 */
struct LineInfo line_next(struct LineInfo line, struct utf8 utf8
                          , int32_t width, float scale, double letterSpacing
                          , const stbtt_fontinfo* font, uint32_t wrapmode)
{
    line = line_advance(line, utf8);
    return line_peek(line, utf8, width, scale, letterSpacing, font, wrapmode);
}
