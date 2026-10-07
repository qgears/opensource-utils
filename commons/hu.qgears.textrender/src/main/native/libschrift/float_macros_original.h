#define integer_long long
#define SCHRIFT_F32 float
#define SCHRIFT_F64 double
#define SCHRIFT_F64_div(a,b)a/b
#define SCHRIFT_F64_from_s32(a)a
#define SCHRIFT_F64_cast_from_s32(a)(double)a
#define SCHRIFT_F64_neg(a)-a
#define SCHRIFT_F64_pos(a)+a
#define SCHRIFT_F64_mul(a,b)a*b
#define SCHRIFT_F64_add(a,b)a+b
#define SCHRIFT_F64_sub(a,b)a-b
#define SCHRIFT_F64_addEq(a,b)a+=b
#define SCHRIFT_F64_subEq(a,b)a-=b
#define SCHRIFT_F64_const_half 0.5
#define SCHRIFT_F64_const_zero 0.0
#define SCHRIFT_F64_const_16384 16384.0
#define SCHRIFT_F64_const_1 1.0
#define SCHRIFT_F64_const_2 2.0
#define SCHRIFT_F64_const_255 255.0
#define SCHRIFT_F64_const_100 100.0
#define SCHRIFT_F64_compare_lt(a,b)a<b
#define SCHRIFT_F64_compare_gteq(a,b)a>=b
#define SCHRIFT_F64_nextafter(a,b)nextafter(a,b)
#define SCHRIFT_F64_floor_to_int(a)(int) floor(a)
#define SCHRIFT_F64_ceil_to_int(a)(int) ceil (a)
#define SCHRIFT_F64_mul_s32_double(a,b)a*b
#define SCHRIFT_F64_mul_s32_SCHRIFT_F64(a,b)a*b
#define SCHRIFT_F64_from_long(a)(double)a
#define SCHRIFT_F64_abs(a)fabs(a)
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
#define SCHRIFT_F64_from_u16_fast(a)a
#define SCHRIFT_F64_cast_to_s32(f)f
#define SCHRIFT_F32_from_float(f)f
#define SCHRIFT_F64_from_double(d)d



