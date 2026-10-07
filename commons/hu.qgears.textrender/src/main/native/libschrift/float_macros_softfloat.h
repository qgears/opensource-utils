#ifndef SOFTFLOAT_SOFTFLOAT_H_
#define SOFTFLOAT_SOFTFLOAT_H_

#include <stdbool.h>
#include <math.h>
typedef uint32_t u32;
typedef uint64_t u64;
typedef int32_t s32;
typedef int64_t s64;
#define SOFTFLOAT_LIB_TEST
#include "softfloat.h"


#define integer_long int32_t
#define SOFTFLOAT_32 S_F32
#define SOFTFLOAT_64 S_F32
#define SOFTFLOAT_64_div(a,b)S_F32_div_TEST(a,b)
#define SOFTFLOAT_32_trunc_to_u8_to_u32(f) (uint32_t)(uint8_t)S_F32_trunc_to_s32_TEST(f)
#define SOFTFLOAT_32_mul_u32(a,b)S_F32_mul_TEST(a,S_F32_from_s64_TEST((s64)(u32)b))
#define SOFTFLOAT_64_from_SOFTFLOAT_32(f)f
#define SOFTFLOAT_64_const_zero S_F32_from_s64_TEST(0)
#define SOFTFLOAT_64_const_1 S_F32_from_s64_TEST(1)
#define SOFTFLOAT_64_const_255 S_F32_from_s64_TEST(255)
#define SOFTFLOAT_64_const_half S_F32_div_TEST(S_F32_from_s64_TEST(1),S_F32_from_s64_TEST(2))
#define SOFTFLOAT_64_trunc_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SOFTFLOAT_64_add(a,b) S_F32_add_TEST(a,b)
#define SOFTFLOAT_64_cast_from_s32(a) S_F32_from_s64_TEST(a)
#define SOFTFLOAT_const_100 S_F32_from_s64_TEST(100)
#define SOFTFLOAT_64_const_16384 S_F32_from_s64_TEST(16384)
#define SOFTFLOAT_64_const_2 S_F32_from_s64_TEST(2)
#define SOFTFLOAT_64_to_double(f) (double)S_F32_toFloat(f)
#define SOFTFLOAT_64_trunc_cast_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SOFTFLOAT_64_sub(a,b) S_F32_add_TEST(a,S_F32_mul_TEST(b,S_F32_from_s64_TEST(-1)))
#define SOFTFLOAT_64_mul(a,b) S_F32_mul_TEST(a,b)
#define SOFTFLOAT_64_from_s32(a) S_F32_from_s64_TEST(a)
#define SOFTFLOAT_64_addEq(a,b) a=S_F32_add_TEST(a,b)
#define SOFTFLOAT_64_subEq(a,b) a=S_F32_add_TEST(a,S_F32_mul_TEST(b,S_F32_from_s64_TEST(-1)))
#define SOFTFLOAT_64_signum(a) S_F32_signum_TEST(a)
#define SOFTFLOAT_64_compare_lt(a,b) (S_F32_compare_TEST(a,b)<0)
#define SOFTFLOAT_64_from_u16_fast(a) S_F32_from_s64_TEST((uint32_t)(uint16_t)a)
#define SOFTFLOAT_64_neg(f) S_F32_mul(f,S_F32_from_s64_TEST(-1))
#define SOFTFLOAT_64_max(a,b) (S_F32_compare_TEST(a,b)>0?a:b)
#define SOFTFLOAT_64_min(a,b) (S_F32_compare_TEST(a,b)<0?a:b)
#define SOFTFLOAT_64_cast_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SOFTFLOAT_64_from_long(l) S_F32_from_s64_TEST(l)
#define SOFTFLOAT_64_pos(f) f
#define SOFTFLOAT_64_return_fast_floor(f) return S_F32_floor_to_s32_TEST(f)
#define SOFTFLOAT_64_return_fast_ceil(f) return S_F32_ceil_to_s32_TEST(f)
#define SOFTFLOAT_64_compare_gteq(a,b) (S_F32_compare_TEST(a,b)>=0)
#define SOFTFLOAT_64_compare_lteq(a,b) (S_F32_compare_TEST(a,b)<=0)
#define SOFTFLOAT_64_floor_to_int(f) S_F32_floor_to_s32_TEST(f)
#define SOFTFLOAT_64_ceil_to_int(f) S_F32_ceil_to_s32_TEST(f)
#define SOFTFLOAT_64_mul_s32_SOFTFLOAT_64(ia, fb) S_F32_mul_TEST(S_F32_from_s64_TEST(ia),fb)
#define SOFTFLOAT_64_abs(f) S_F32_mul_TEST(f, S_F32_from_s64_TEST(S_F32_signum_TEST(f)))
#define SOFTFLOAT_64_nextafter(a,b) S_F32_nextafter_TEST(a,b)

#endif

