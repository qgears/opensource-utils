#ifndef SOFTFLOAT_SOFTFLOAT_H_
#define SOFTFLOAT_SOFTFLOAT_H_

#include <stdbool.h>
#include <math.h>
typedef uint32_t u32;
typedef uint64_t u64;
typedef int32_t s32;
typedef int64_t s64;
#include "softfloat_impl.h"


#define integer_long int32_t
#define SOFTFLOAT_32 S_F32
#define SOFTFLOAT_64 S_F32
#define SOFTFLOAT_64_div(a,b)S_F32_div(a,b)
#define SOFTFLOAT_32_trunc_to_u8_to_u32(f) (uint32_t)(uint8_t)S_F32_trunc_to_s32(f)
#define SOFTFLOAT_32_mul_u32(a,b)S_F32_mul(a,S_F32_from_s64((s64)(u32)b))
#define SOFTFLOAT_64_from_SOFTFLOAT_32(f)f
#define SOFTFLOAT_64_const_zero S_F32_from_s64(0)
#define SOFTFLOAT_64_const_1 S_F32_from_s64(1)
#define SOFTFLOAT_64_const_255 S_F32_from_s64(255)
#define SOFTFLOAT_64_const_half S_F32_div(S_F32_from_s64(1),S_F32_from_s64(2))
#define SOFTFLOAT_64_trunc_to_s32(f) S_F32_trunc_to_s32(f)
#define SOFTFLOAT_64_add(a,b) S_F32_add(a,b)
#define SOFTFLOAT_64_cast_from_s32(a) S_F32_from_s64(a)
#define SOFTFLOAT_const_100 S_F32_from_s64(100)
#define SOFTFLOAT_64_const_16384 S_F32_from_s64(16384)
#define SOFTFLOAT_64_const_2 S_F32_from_s64(2)
#define SOFTFLOAT_64_to_double(f) (double)S_F32_toFloat(f)
#define SOFTFLOAT_64_trunc_cast_to_s32(f) S_F32_trunc_to_s32(f)
#define SOFTFLOAT_64_sub(a,b) S_F32_add(a,S_F32_mul(b,S_F32_from_s64(-1)))
#define SOFTFLOAT_64_mul(a,b) S_F32_mul(a,b)
#define SOFTFLOAT_64_from_s32(a) S_F32_from_s64(a)
#define SOFTFLOAT_64_addEq(a,b) a=S_F32_add(a,b)
#define SOFTFLOAT_64_subEq(a,b) a=S_F32_add(a,S_F32_mul(b,S_F32_from_s64(-1)))
#define SOFTFLOAT_64_signum(a) S_F32_signum(a)
#define SOFTFLOAT_64_compare_lt(a,b) (S_F32_compare(a,b)<0)
#define SOFTFLOAT_64_from_u16_fast(a) S_F32_from_s64((uint32_t)(uint16_t)a)
#define SOFTFLOAT_64_neg(f) S_F32_mul(f,S_F32_from_s64(-1))
#define SOFTFLOAT_64_max(a,b) (S_F32_compare(a,b)>0?a:b)
#define SOFTFLOAT_64_min(a,b) (S_F32_compare(a,b)<0?a:b)
#define SOFTFLOAT_64_cast_to_s32(f) S_F32_trunc_to_s32(f)
#define SOFTFLOAT_64_from_long(l) S_F32_from_s64(l)
#define SOFTFLOAT_64_pos(f) f
#define SOFTFLOAT_64_return_fast_floor(f) return S_F32_floor_to_s32(f)
#define SOFTFLOAT_64_return_fast_ceil(f) return S_F32_ceil_to_s32(f)
#define SOFTFLOAT_64_compare_gteq(a,b) (S_F32_compare(a,b)>=0)
#define SOFTFLOAT_64_compare_lteq(a,b) (S_F32_compare(a,b)<=0)
#define SOFTFLOAT_64_floor_to_int(f) S_F32_floor_to_s32(f)
#define SOFTFLOAT_64_ceil_to_int(f) S_F32_ceil_to_s32(f)
#define SOFTFLOAT_64_mul_s32_SOFTFLOAT_64(ia, fb) S_F32_mul(S_F32_from_s64(ia),fb)
#define SOFTFLOAT_64_abs(f) S_F32_mul(f, S_F32_from_s64(S_F32_signum(f)))
#define SOFTFLOAT_64_nextafter(a,b) S_F32_nextafter(a,b)
/*
#define SOFTFLOAT_64_from_s32(a)a
#define SOFTFLOAT_64_cast_from_s32(a)(double)a
#define SOFTFLOAT_64_neg(a)-a
#define SOFTFLOAT_64_pos(a)+a
#define SOFTFLOAT_64_mul(a,b)a*b
#define SOFTFLOAT_64_add(a,b)a+b
#define SOFTFLOAT_64_sub(a,b)a-b
#define SOFTFLOAT_64_addEq(a,b)a+=b
#define SOFTFLOAT_64_subEq(a,b)a-=b
#define SOFTFLOAT_64_const_half 0.5
#define SOFTFLOAT_64_const_zero 0.0
#define SOFTFLOAT_64_const_16384 16384.0
#define SOFTFLOAT_64_const_1 1.0
#define SOFTFLOAT_64_const_2 2.0
#define SOFTFLOAT_64_const_255 255.0
#define SOFTFLOAT_const_100 100.0
#define SOFTFLOAT_64_compare_lt(a,b)a<b
#define SOFTFLOAT_64_compare_gteq(a,b)a>=b
#define SOFTFLOAT_64_nextafter(a,b)nextafter(a,b)
#define SOFTFLOAT_64_floor_to_int(a)(int) floor(a)
#define SOFTFLOAT_64_ceil_to_int(a)(int) ceil (a)
#define SOFTFLOAT_64_mul_s32_double(a,b)a*b
#define SOFTFLOAT_64_mul_s32_SOFTFLOAT_64(a,b)a*b
#define SOFTFLOAT_64_from_long(a)(double)a
#define SOFTFLOAT_64_abs(a)fabs(a)
#define SOFTFLOAT_64_compare_lteq(a,b)a<=b
#define SOFTFLOAT_64_signum(a)SIGN(a)
#define SOFTFLOAT_64_min(a,b)MIN(a,b)
#define SOFTFLOAT_64_floor_to_s32(a)a
#define SOFTFLOAT_64_return_fast_floor(x)int i = (int) x; return i - (i > x)
#define SOFTFLOAT_64_return_fast_ceil(x)int i = (int) x; return i + (i < x)

static inline uint32_t SOFTFLOAT_32_floor_to_u32(SOFTFLOAT_32 f) {return (uint32_t)floor(f);}
static inline SOFTFLOAT_32 SOFTFLOAT_32_from_float(float f)  {return f;}
static inline SOFTFLOAT_32 SOFTFLOAT_32_mul_u32(SOFTFLOAT_32 f, uint32_t u) {return f*(float)u;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_SOFTFLOAT_32(SOFTFLOAT_32 v) {return (double) v;}
static inline double SOFTFLOAT_64_to_double(SOFTFLOAT_64 f)  {return f;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_from_u16_fast(uint_fast16_t v) {return (double) v;}
static inline SOFTFLOAT_64 SOFTFLOAT_64_max(SOFTFLOAT_64 a, SOFTFLOAT_64 b)  {return fmax(a,b);}
static inline int32_t SOFTFLOAT_64_cast_to_s32(SOFTFLOAT_64 f) {return (int32_t) f;}
#define SOFTFLOAT_32_floor_to_u8_to_u32(a)(uint32_t)(((uint8_t)a))
#define SOFTFLOAT_64_floor_cast_to_s32(a)(int32_t)a
*/

#endif

