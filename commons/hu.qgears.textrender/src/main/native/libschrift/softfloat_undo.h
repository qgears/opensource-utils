#define integer_long long
#define SOFTFLOAT_32 float
#define SOFTFLOAT_64 double
#define SOFTFLOAT_64_div(a,b)a/b
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
#define SOFTFLOAT_64_max(a,b)MAX(a,b)
#define SOFTFLOAT_64_trunc_to_s32(a)a
//#define SOFTFLOAT_64_floor_to_s32(a)a
#define SOFTFLOAT_64_trunc_cast_to_s32(a)(int32_t)a
#define SOFTFLOAT_64_return_fast_floor(x)int i = (int) x; return i - (i > x)
#define SOFTFLOAT_64_return_fast_ceil(x)int i = (int) x; return i + (i < x)
#define SOFTFLOAT_64_from_SOFTFLOAT_32(f)f
#define SOFTFLOAT_64_to_double(f)f
#define SOFTFLOAT_32_mul_u32(a,b)a*b
#define SOFTFLOAT_32_trunc_to_u8_to_u32(a)(uint32_t)(((uint8_t)a))
#define SOFTFLOAT_64_from_u16_fast(a)a
#define SOFTFLOAT_64_cast_to_s32(f)f



