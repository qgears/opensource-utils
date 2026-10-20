#include <stdint.h>

typedef int32_t fix32_t;

static inline fix32_t fix32FractionalPart(fix32_t f) {
    return f & 0xFF;
}

static inline fix32_t fix32WholePart(fix32_t f) {
    return f & ~0xFF;
}

static inline int32_t fix32ToInt32(fix32_t f) {
    return f >> 8;
}

static inline int32_t fix32FromInt32(int32_t i) {
    return i << 8;
}

static inline double fix32ToDouble(fix32_t f) {
    return (double) f / 0x100;
}

static inline fix32_t fix32FromDouble(double d) {
    //rounds down
    return (fix32_t) (d * 0x100);
}