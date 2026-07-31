#ifndef INCLUDED_QSTB_CTYPE_H
#define INCLUDED_QSTB_CTYPE_H

#include <stdbool.h>
#include <stdint.h>

typedef uint32_t char32_t;
bool is_space(char32_t c);
bool is_control(char32_t c);
bool is_blank(char32_t c);
bool is_graph(char32_t c);
bool is_print(char32_t c);
bool is_line_ending(char32_t c);
bool is_ignore(char32_t c);

#endif
