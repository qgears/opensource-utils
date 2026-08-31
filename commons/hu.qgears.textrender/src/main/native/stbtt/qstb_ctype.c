#include <stdbool.h>
#include <stdint.h>

typedef uint32_t char32_t;
bool is_space(char32_t c) {
    return c == ' ';
}
bool is_control(char32_t c) {
    //includes \t \r \n \0
    return c < 0x20u || c == 0x7Fu;
}
bool is_blank(char32_t c) {
    return c == ' ' || c == '\t';
}
bool is_graph(char32_t c) {
    return !is_blank(c) && !is_control(c);
}
bool is_print(char32_t c) {
    return is_graph(c) || is_space(c);
}
bool is_line_ending(char32_t c) {
    return c == '\n' || c == '\0';
}
bool is_ignore(char32_t c) {
    return !(is_print(c) || is_line_ending(c));
}
