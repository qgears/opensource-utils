#ifndef SOFTFLOAT_H_
#define SOFTFLOAT_H_

#include <stdbool.h>

/// TODO according to C spec long is minimum 32 bit. But on most platforms it is 64 bit. Review if 32 bit is enough or not?
typedef int32_t integer_long;
typedef struct{uint8_t a[4];}  SOFTFLOAT_32;
typedef struct{uint8_t a[8];} SOFTFLOAT_64;

/// TODO usage should be explicite whether round, floor, etc.
int32_t SOFTFLOAT_64_cast_to_s32(SOFTFLOAT_64 f);
int32_t SOFTFLOAT_64_floor_to_s32(SOFTFLOAT_64 f);
uint32_t SOFTFLOAT_32_floor_to_u32(SOFTFLOAT_32 f);
int32_t SOFTFLOAT_64_floor_to_s32(SOFTFLOAT_64 f);
int32_t SOFTFLOAT_64_ceil_to_s32(SOFTFLOAT_64 f);
SOFTFLOAT_32 SOFTFLOAT_32_from_float(float f);
SOFTFLOAT_32 SOFTFLOAT_32_mul_u32(SOFTFLOAT_32 f, uint32_t u);
SOFTFLOAT_64 SOFTFLOAT_64_from_double(double f);
double SOFTFLOAT_64_to_double(SOFTFLOAT_64 f);
SOFTFLOAT_64 SOFTFLOAT_64_from_u16_fast(uint_fast16_t v);
SOFTFLOAT_64 SOFTFLOAT_64_from_s32(int32_t v);
/// TODO decide whether long is 32 or 64 bit!
SOFTFLOAT_64 SOFTFLOAT_64_from_long(long v);
SOFTFLOAT_64 SOFTFLOAT_64_from_SOFTFLOAT_32(SOFTFLOAT_32 v);
SOFTFLOAT_64 SOFTFLOAT_64_sub (SOFTFLOAT_64 a, SOFTFLOAT_64 b);
SOFTFLOAT_64 SOFTFLOAT_64_add (SOFTFLOAT_64 a, SOFTFLOAT_64 b);
SOFTFLOAT_64 SOFTFLOAT_64_mul (SOFTFLOAT_64 a, SOFTFLOAT_64 b);
SOFTFLOAT_64 SOFTFLOAT_64_div (SOFTFLOAT_64 a, SOFTFLOAT_64 b);
SOFTFLOAT_64 SOFTFLOAT_64_neg (SOFTFLOAT_64 a);
SOFTFLOAT_64 SOFTFLOAT_64_abs (SOFTFLOAT_64 a);
/// a<0?-1:(a>0?1:0)
int32_t SOFTFLOAT_64_signum (SOFTFLOAT_64 a);


SOFTFLOAT_64 SOFTFLOAT_64_mul_s32_SOFTFLOAT_64(int32_t a, SOFTFLOAT_64 b);
/// a < b
bool SOFTFLOAT_64_compare_lt(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// a>=b
bool SOFTFLOAT_64_compare_gteq(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// a<=b
bool SOFTFLOAT_64_compare_lteq(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// max(a,b);
SOFTFLOAT_64 SOFTFLOAT_64_max(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// min(a,b);
SOFTFLOAT_64 SOFTFLOAT_64_min(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// nextafter(a,b);
SOFTFLOAT_64 SOFTFLOAT_64_nextafter(SOFTFLOAT_64 a, SOFTFLOAT_64 b);
/// TODO very much not optimal if it is not const folded by the compiler.
#define SOFTFLOAT_64_const_half (SOFTFLOAT_64_div(SOFTFLOAT_64_from_s32(1), SOFTFLOAT_64_from_s32(2)))
#define SOFTFLOAT_64_const_zero (SOFTFLOAT_64_from_s32(0))
/// TODO div by 16384 is simply sub from exp
#define SOFTFLOAT_64_const_16384 (SOFTFLOAT_64_from_s32(16384))
#define SOFTFLOAT_64_const_1 (SOFTFLOAT_64_from_s32(1))
#define SOFTFLOAT_64_const_2 (SOFTFLOAT_64_from_s32(2))
#define SOFTFLOAT_64_const_255 (SOFTFLOAT_64_from_s32(255))
#endif

