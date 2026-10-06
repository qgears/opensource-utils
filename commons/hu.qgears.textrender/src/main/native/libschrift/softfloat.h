#ifndef SOFTFLOAT_H_
#define SOFTFLOAT_H_

#include <stdbool.h>
#include <math.h>

/// TODO according to C spec long is minimum 32 bit. But on most platforms it is 64 bit. Review if 32 bit is enough or not?
typedef int32_t integer_long;
typedef float  SOFTFLOAT_32;
typedef double SOFTFLOAT_64;

/// TODO usage should be explicite whether round, floor, etc.
static inline int32_t SOFTFLOAT_64_cast_to_s32(SOFTFLOAT_64 f) {return (int32_t) f;}
static inline int32_t SOFTFLOAT_64_floor_to_s32(SOFTFLOAT_64 f) {return (int32_t)floor(f);}
static inline uint32_t SOFTFLOAT_32_floor_to_u32(SOFTFLOAT_32 f) {return (uint32_t)floor(f);}
static inline int32_t SOFTFLOAT_64_ceil_to_s32(SOFTFLOAT_64 f) {return (int32_t)ceil(f);}
static inline SOFTFLOAT_32 SOFTFLOAT_32_from_float(float f)  {return f;}
static inline SOFTFLOAT_32 SOFTFLOAT_32_mul_u32(SOFTFLOAT_32 f, uint32_t u) {return f*(float)u;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_double(double f)  {return f;}
static inline double SOFTFLOAT_64_to_double(SOFTFLOAT_64 f)  {return f;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_u16_fast(uint_fast16_t v) {return (double) v;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_s32(int32_t v) {return (double) v;}
/// TODO decide whether long is 32 or 64 bit!
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_long(long v) {return (double) v;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_SOFTFLOAT_32(SOFTFLOAT_32 v) {return (double) v;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_sub (SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a-b;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_add (SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a+b;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_mul (SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a*b;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_div (SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a/b;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_neg (SOFTFLOAT_64 a) {return -a;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_abs (SOFTFLOAT_64 a) {return fabs(a);}
/// a<0?-1:(a>0?1:0)
static inline int32_t SOFTFLOAT_64_signum (SOFTFLOAT_64 a) {return (a<0) ? -1:(a>0?1:0);}


static inline SOFTFLOAT_64 SOFTFLOAT_64_mul_s32_SOFTFLOAT_64(int32_t a, SOFTFLOAT_64 b) {return a*b;}
/// a < b
static inline bool SOFTFLOAT_64_compare_lt(SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a<b;}
/// a>=b
static inline bool SOFTFLOAT_64_compare_gteq(SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a>=b;}
/// a<=b
static inline bool SOFTFLOAT_64_compare_lteq(SOFTFLOAT_64 a, SOFTFLOAT_64 b) {return a<=b;}
/// max(a,b);
static inline SOFTFLOAT_64 SOFTFLOAT_64_max(SOFTFLOAT_64 a, SOFTFLOAT_64 b)  {return fmax(a,b);}
/// min(a,b);
static inline SOFTFLOAT_64 SOFTFLOAT_64_min(SOFTFLOAT_64 a, SOFTFLOAT_64 b)  {return fmin(a,b);}
/// nextafter(a,b);
static inline SOFTFLOAT_64 SOFTFLOAT_64_nextafter(SOFTFLOAT_64 a, SOFTFLOAT_64 b)  {return nextafter(a,b);}
/// TODO very much not optimal if it is not const folded by the compiler.
#define SOFTFLOAT_64_const_half (SOFTFLOAT_64_div(SOFTFLOAT_64_from_s32(1), SOFTFLOAT_64_from_s32(2)))
#define SOFTFLOAT_64_const_zero (SOFTFLOAT_64_from_s32(0))
/// TODO div by 16384 is simply sub from exp
#define SOFTFLOAT_64_const_16384 (SOFTFLOAT_64_from_s32(16384))
#define SOFTFLOAT_64_const_1 (SOFTFLOAT_64_from_s32(1))
#define SOFTFLOAT_64_const_2 (SOFTFLOAT_64_from_s32(2))
#define SOFTFLOAT_64_const_255 (SOFTFLOAT_64_from_s32(255))
#endif

