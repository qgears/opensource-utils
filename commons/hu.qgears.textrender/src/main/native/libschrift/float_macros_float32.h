#ifndef FLOAT_MACROS_ORIGINAL_H
#define FLOAT_MACROS_ORIGINAL_H

/**
 * This version converts back all floating point operations and types
 * to a 32 bit representation (double downcasted to float)
 */

#include <stdint.h>

/* libm-free replacements, results are bit-identical to floor/ceil/fabs/nextafterf */
static inline int schrift_f32_floor_to_int(float x)
{
	int i = (int) x;
	return i - ((float) i > x);
}

static inline int schrift_f32_ceil_to_int(float x)
{
	int i = (int) x;
	return i + ((float) i < x);
}

static inline float schrift_f32_nextafter(float x, float y)
{
	union { float f; uint32_t u; } v;
	if (__builtin_isnanf(x) || __builtin_isnanf(y)) {
		return x + y;
	}
	if (x == y) {
		return y;
	}
	if (x == 0.0f) {
		v.u = 1u;
		return y > 0.0f ? v.f : -v.f;
	}
	v.f = x;
	if ((x < y) == (x > 0.0f)) {
		v.u++;
	} else {
		v.u--;
	}
	return v.f;
}

static inline float schrift_f32_ceil_to_Zper0x1p8(float x) {
	/**
	 * Alternatives:
	 * a) ldexp(f(ldexp(x, -8)), 8)
	 * 		where f is one of: ceil, floor etc.
	 * 		These are not available without math.h
	 * 		A clean implementation would include bit manipulation
	 * b) direct bit manipulation
	 * 		check the exponent, to understand the place value of the bits of the mantissa
	 * 		manipulate the mantissa directly
	 * c) using fmodf(x, 0x1p-8f), then relying on arithmetic
	 * 		A clean implementation of fmodf would include bit manipulation
	 */

	if (__builtin_isnanf(x) || __builtin_isinff(x)) {
		return x;
	}

	/**
	 * The mantissa of a float32 is 23 bits wide (plus the implicit 1)
	 * From 0x1p15f and upward, there is no binary digit with place value smaller than 0x1p-8f
	 * Our calculation will comfortably represent 0x1p15f and smaller numbers, as an int32, using up to 24 bits
	 */
	if (-0x1p15f < x && x < 0x1p15f) {
		float x2 = x * 0x1p8f;
		int32_t i = (int32_t) x2;
		return (i + ((float) i < x2)) * 0x1p-8f;
	}

	//don't care, best-effort
	return x;
}

static inline float schrift_f32_floor_to_Zper0x1p8(float x) {
	if (__builtin_isnanf(x) || __builtin_isinff(x)) {
		return x;
	}

	if (-0x1p15f < x && x < 0x1p15f) {
		float x2 = x * 0x1p8f;
		int32_t i = (int32_t) x2;
		return (i - ((float) i > x2)) * 0x1p-8f;
	}

	//don't care, best-effort
	return x;
}

#define integer_long long
#define SCHRIFT_F32 float
#define SCHRIFT_F64 float
#define SCHRIFT_F64_div(a,b)a/b
#define SCHRIFT_F64_from_s32(a)(float)a
#define SCHRIFT_F64_cast_from_u32(a)(float)a
#define SCHRIFT_F64_cast_from_s32(a)(float)a
#define SCHRIFT_F64_neg(a)-a
#define SCHRIFT_F64_pos(a)+a
#define SCHRIFT_F64_mul(a,b)a*b
#define SCHRIFT_F64_add(a,b)a+b
#define SCHRIFT_F64_sub(a,b)a-b
#define SCHRIFT_F64_addEq(a,b)a+=b
#define SCHRIFT_F64_subEq(a,b)a-=b
#define SCHRIFT_F64_const_half 0.5f
#define SCHRIFT_F64_const_zero 0.0f
#define SCHRIFT_F64_const_16384 16384.0f
#define SCHRIFT_F64_const_1 1.0f
#define SCHRIFT_F64_const_2 2.0f
#define SCHRIFT_F64_const_255 255.0f
#define SCHRIFT_F64_const_100 100.0f
#define SCHRIFT_F64_compare_lt(a,b)a<b
#define SCHRIFT_F64_compare_gteq(a,b)a>=b
#define SCHRIFT_F64_nextafter(a,b) schrift_f32_nextafter(a,b)
#define SCHRIFT_F64_floor_to_int(a)schrift_f32_floor_to_int(a)
#define SCHRIFT_F64_ceil_to_int(a)schrift_f32_ceil_to_int(a)
#define SCHRIFT_F64_mul_s32_double(a,b)a*b
#define SCHRIFT_F64_mul_s32_SCHRIFT_F64(a,b)(float)a*b
#define SCHRIFT_F64_from_long(a)(float)a
#define SCHRIFT_F64_abs(a)__builtin_fabsf(a)
#define SCHRIFT_F64_compare_lteq(a,b)a<=b
#define SCHRIFT_F64_signum(a)SIGN(a)
#define SCHRIFT_F64_min(a,b)MIN(a,b)
#define SCHRIFT_F64_max(a,b)MAX(a,b)
#define SCHRIFT_F64_trunc_to_s32(a)a
//#define SCHRIFT_F64_floor_to_s32(a)a
#define SCHRIFT_F64_trunc_cast_to_s32(a)(int32_t)a
#define SCHRIFT_F64_return_fast_floor(x)int i = (int) x; return i - (i > x)
#define SCHRIFT_F64_return_fast_ceil(x)int i = (int) x; return i + (i < x)
#define SCHRIFT_F64_from_SCHRIFT_F32(f)f
#define SCHRIFT_F64_to_double(f)f
#define SCHRIFT_F32_mul_u32(a,b)a*b
#define SCHRIFT_F32_trunc_to_u8_to_u32(a)(uint32_t)(((uint8_t)a))
#define SCHRIFT_F64_from_u16_fast(a)(float)(uint16_t)(a)
#define SCHRIFT_F64_cast_to_s32(f)(int32_t)(f)
#define SCHRIFT_F32_from_float(f)f
#define SCHRIFT_F64_from_double(d)(float)d
#define SCHRIFT_F64_floor_to_Zper0x1p8(d) schrift_f32_floor_to_Zper0x1p8(d)
#define SCHRIFT_F64_ceil_to_Zper0x1p8(d) schrift_f32_ceil_to_Zper0x1p8(d)
#define SCHRIFT_F32_floor_to_Zper0x1p8(f) schrift_f32_floor_to_Zper0x1p8(f)
#define SCHRIFT_F32_ceil_to_Zper0x1p8(f) schrift_f32_ceil_to_Zper0x1p8(f)

#endif /* FLOAT_MACROS_ORIGINAL_H */



