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
#define SCHRIFT_F32 S_F32
#define SCHRIFT_F64 S_F32
#define SCHRIFT_F64_div(a,b)S_F32_div_TEST(a,b)
#define SCHRIFT_F32_trunc_to_u8_to_u32(f) (uint32_t)(uint8_t)S_F32_trunc_to_s32_TEST(f)
#define SCHRIFT_F32_mul_u32(a,b)S_F32_mul_TEST(a,S_F32_from_s64_TEST((s64)(u32)b))
#define SCHRIFT_F64_from_SCHRIFT_F32(f)f
#define SCHRIFT_F64_const_zero S_F32_from_s64_TEST(0)
#define SCHRIFT_F64_const_1 S_F32_from_s64_TEST(1)
#define SCHRIFT_F64_const_255 S_F32_from_s64_TEST(255)
#define SCHRIFT_F64_const_half S_F32_div_TEST(S_F32_from_s64_TEST(1),S_F32_from_s64_TEST(2))
#define SCHRIFT_F64_trunc_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SCHRIFT_F64_add(a,b) S_F32_add_TEST(a,b)
#define SCHRIFT_F64_cast_from_s32(a) S_F32_from_s64_TEST(a)
#define SCHRIFT_F64_const_100 S_F32_from_s64_TEST(100)
#define SCHRIFT_F64_const_16384 S_F32_from_s64_TEST(16384)
#define SCHRIFT_F64_const_2 S_F32_from_s64_TEST(2)
#define SCHRIFT_F64_to_double(f) (double)S_F32_toFloat(f)
#define SCHRIFT_F64_trunc_cast_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SCHRIFT_F64_sub(a,b) S_F32_add_TEST(a,S_F32_mul_TEST(b,S_F32_from_s64_TEST(-1)))
#define SCHRIFT_F64_mul(a,b) S_F32_mul_TEST(a,b)
#define SCHRIFT_F64_from_s32(a) S_F32_from_s64_TEST(a)
#define SCHRIFT_F64_addEq(a,b) a=S_F32_add_TEST(a,b)
#define SCHRIFT_F64_subEq(a,b) a=S_F32_add_TEST(a,S_F32_mul_TEST(b,S_F32_from_s64_TEST(-1)))
#define SCHRIFT_F64_signum(a) S_F32_signum_TEST(a)
#define SCHRIFT_F64_compare_lt(a,b) (S_F32_compare_TEST(a,b)<0)
#define SCHRIFT_F64_from_u16_fast(a) S_F32_from_s64_TEST((uint32_t)(uint16_t)a)
#define SCHRIFT_F64_neg(f) S_F32_mul(f,S_F32_from_s64_TEST(-1))
#define SCHRIFT_F64_max(a,b) (S_F32_compare_TEST(a,b)>0?a:b)
#define SCHRIFT_F64_min(a,b) (S_F32_compare_TEST(a,b)<0?a:b)
#define SCHRIFT_F64_cast_to_s32(f) S_F32_trunc_to_s32_TEST(f)
#define SCHRIFT_F64_from_long(l) S_F32_from_s64_TEST(l)
#define SCHRIFT_F64_pos(f) f
#define SCHRIFT_F64_return_fast_floor(f) return S_F32_floor_to_s32_TEST(f)
#define SCHRIFT_F64_return_fast_ceil(f) return S_F32_ceil_to_s32_TEST(f)
#define SCHRIFT_F64_compare_gteq(a,b) (S_F32_compare_TEST(a,b)>=0)
#define SCHRIFT_F64_compare_lteq(a,b) (S_F32_compare_TEST(a,b)<=0)
#define SCHRIFT_F64_floor_to_int(f) S_F32_floor_to_s32_TEST(f)
#define SCHRIFT_F64_ceil_to_int(f) S_F32_ceil_to_s32_TEST(f)
#define SCHRIFT_F64_mul_s32_SCHRIFT_F64(ia, fb) S_F32_mul_TEST(S_F32_from_s64_TEST(ia),fb)
#define SCHRIFT_F64_abs(f) S_F32_mul_TEST(f, S_F32_from_s64_TEST(S_F32_signum_TEST(f)))
#define SCHRIFT_F64_nextafter(a,b) S_F32_nextafter_TEST(a,b)

#endif

