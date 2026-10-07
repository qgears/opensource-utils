#ifndef SOFTFLOAT_LIB_H_
#define SOFTFLOAT_LIB_H_
/* Software float library aims to be IEEE 754 compatible 32 bit float. */


/// TODO: infinity, subnormal, +-0 implementations
/// TODO: how to handle div by zero?

/// 32 bit float is stored as an unsigned integer
typedef u32 S_F32;

/// Get fields of a float value: sign bit
static inline u32 S_F32_getSign(S_F32 v)
{
	return (v>>31)&1;
}
/// Get fields of a float value: exponent. @return the exponent value on the -127->128 range. 0 means the implicite 1 of the mantissa means 1.0f.
static inline s32 S_F32_getExp(S_F32 v)
{
	return ((s32)((v>>23)&0xFF))-127;
}
/// Get fields of a float value: mantissa. @return the mantissa as stored. Does not return the implicite 1 on the position 23bit
static inline u32 S_F32_getMantissa(S_F32 v)
{
	return v&((1<<23)-1);
}
/// Create a f32 value by storing its fields: sign, exponent, mantissa
/// @param exp is on the range -127- +128 range. +127 is added before storing it.
static inline S_F32 S_F32_create(u32 sign, s32 exp, u32 mantissa)
{
	u32 signSH=((sign&1)<<31);
	u32 expUns=(u32)(exp+127);
	u32 expSH=((expUns&0xFF)<<23);
	u32 mantSH=(mantissa&((1<<23)-1));
	#ifdef S_F32_LOG
	printf("sign: %d expSH: %d mant: %d\n", signSH, expSH, mantSH);
	#endif
	S_F32 ret=signSH | expSH | mantSH;
	return ret;
}
/// Convert an S_F32 value to a native float.
/// TODO only works if the native float storage is the same as implemented by S_F32 which is normally true.
static inline float S_F32_toFloat(S_F32 v)
{
	union{S_F32 v; float f;} u;
	u.v=v;
	return u.f;
}
/// Convert a native float to S_F32.
/// TODO only works if the native float storage is the same as implemented by S_F32 which is normally true.
static inline S_F32 S_F32_fromFloat(float f)
{
	union{S_F32 v; float f;} u;
	u.f=f;
	return u.v;
}
/// u32 max function for convenience
static inline u32 S_F32_u32_max(u32 a, u32 b)
{
	return a>b?a:b;
}
/// s32 max function for convenience
static inline s32 S_F32_s32_max(s32 a, s32 b)
{
	return a>b?a:b;
}
/// Check if the floating point value is 0 (+0 or -0)
static inline bool S_F32_isZero(S_F32 a)
{
	return S_F32_getExp(a)==-127 && S_F32_getMantissa(a)==0;
}
/// @return a+b
static inline S_F32 S_F32_add(S_F32 a, S_F32 b)
{
	u32 as=S_F32_getSign(a);
	s32 ae=S_F32_getExp(a);
	s64 am=(s64)S_F32_getMantissa(a);
	u32 bs=S_F32_getSign(b);
	s32 be=S_F32_getExp(b);
	s64 bm=(s64)S_F32_getMantissa(b);
	s32 e=S_F32_s32_max(ae,be);
	if(S_F32_isZero(a))
	{
		//printf("a is zero: %d as: %d ae: %d am: %ld %d\n", a, as, ae, am, ((s32)((a>>23)&0xFF)));
		return b;
	}
	if(S_F32_isZero(b))
	{
		#ifdef S_F32_LOG
		printf("b is zero\n");
		#endif
		return a;
	}
	// Shift up to high 32 bit
	am<<=32;
	bm<<=32;
	// Set implicite 1
	am|=(((u64)1)<<(23+32));
	bm|=(((u64)1)<<(23+32));
	// Find how much shift we need for both mantissa and shift them
	s32 shiftA=32-e+ae;
	s32 shiftB=32-e+be;
	if(shiftA<0)
	{
		if(shiftA<31)
		{
			am=0;
		}else
		{
			am>>-shiftA;
		}
	}
	if(shiftB<0)
	{
		if(shiftB<31)
		{
			bm=0;
		}else
		{
			bm>>-shiftB;
		}
	}
	if(as!=0)
	{
		am=-am;
	}
	if(bs!=0)
	{
		bm=-bm;
	}
	s64 m=am+bm;
	u32 s;
	if(m<0)
	{
		s=1;
		m=-m;
	}else
	{
		s=0;
	}
	#ifdef S_F32_LOG
	printf("am            : ");
	print_binary64(am);
	printf("bm            : ");
	print_binary64(bm);
	printf("Added mantissa: ");
	print_binary64(m);
	#endif
	if((m&(((u64)1)<<31))!=0)
	{
		// Rounding up when first lost bit is 1
		m+=(((u64)1)<<32);
	}
	#ifdef S_F32_LOG
	printf("After rounding: ");
	print_binary64(m);
	#endif
	
	if((m&(((u64)1)<<(24+32)))!=0)
	{
		// Overflow on highest bit
		m>>=1;
		e+=1;
	}
	m>>=23;
	#ifdef S_F32_LOG
	printf("After shifting: ");
	print_binary64(m);
	#endif
	
	return S_F32_create(s, e, (u32)(u64)m);
}
/// @return a*b
static inline S_F32 S_F32_mul(S_F32 a, S_F32 b)
{
	u32 as=S_F32_getSign(a);
	s32 ae=S_F32_getExp(a);
	u64 am=(u64)S_F32_getMantissa(a);
	u32 bs=S_F32_getSign(b);
	s32 be=S_F32_getExp(b);
	u64 bm=(u64)S_F32_getMantissa(b);
	u32 e=ae+be;
	if(S_F32_isZero(a))
	{
		// TODO handle minus0?
		return a;
	}
	if(S_F32_isZero(b))
	{
		// TODO handle minus0?
		return b;
	}
	// Set implicite 1
	am|=(((u64)1)<<(23));
	bm|=(((u64)1)<<(23));
	u64 m=am*bm;
	u32 firstOneIndex=__builtin_clzll(m);
	if(firstOneIndex==17)
	{
		m>>=14;
	}else
	{
		m>>=15;
		e+=1;
	}
	if((m&(((u64)1)<<31))!=0)
	{
		// Rounding up when first lost bit is 1
		m+=(((u64)1)<<32);
	}
	m>>=9;
//	e+=17-firstOneIndex;
	u32 s=as+bs; // Value is cropped by S_F32_create
	#ifdef S_F32_LOG
	printf("mul result: ae: %d be: %d e: %d m: %d\n", ae, be, e, m);
	printf("firstOneIndex: %d\n",firstOneIndex);
	print_binary64(am);
	print_binary64(bm);
	print_binary64(m);
	#endif
	return S_F32_create(s, e, (u32)(u64)m);
}
static inline S_F32 S_F32_from_s64(s64 v)
{
	u64 uv;
	u32 sign;
	if(v==0)
	{
		return S_F32_create(0,-127,0);
	}
	if(v<0)
	{
		uv=(u64)-v;
		sign=1;
	}else
	{
		uv=(u64)v;
		sign=0;
	}
	s32 leftZeros=__builtin_clzll(uv);
	s32 lzTarget=40;
	s32 shift=lzTarget-leftZeros;
	if(shift>0)
	{
		uv>>=shift;
	}
	else if(shift<0)
	{
		uv<<=-shift;
	}
	s32 e=shift+23;
	return S_F32_create(sign, e, uv);
}

/// @return a/b
static inline S_F32 S_F32_div(S_F32 a, S_F32 b)
{
	u32 as=S_F32_getSign(a);
	s32 ae=S_F32_getExp(a);
	u64 am=(u64)S_F32_getMantissa(a);
	u32 bs=S_F32_getSign(b);
	s32 be=S_F32_getExp(b);
	u64 bm=(u64)S_F32_getMantissa(b);
	if(S_F32_isZero(a))
	{
		// TODO handle minus0?
		return a;
	}
	if(S_F32_isZero(b))
	{
		// Throw div 0 exception
		return a/b;
	}
	// Set implicite 1
	am|=(((u64)1)<<(23));
	bm|=(((u64)1)<<(23));
	u64 _div=am/bm;
	u64 _mod=am%bm;
	_div<<=32;
	_mod<<=32;
	u64 _part=_mod/bm;
	u64 m = _div | _part;
	u32 usefulDigits=64-__builtin_clzll(m);
	u32 shift=usefulDigits-33;
	m>>=shift;
	if((m&(((u64)1)<<31))!=0)
	{
		// Rounding up when first lost bit is 1
		m+=(((u64)1)<<32);
	}
	m>>=9;
	u32 e=ae-be-shift;
	u32 s=as+bs; // Value is cropped by S_F32_create
	#ifdef S_F32_LOG
	printf("div result: ae: %d be: %d e: %d m: %d shift: %d\n", ae, be, e, m, shift);
	printf("usefulDigits: %d\n",usefulDigits);
	print_binary64(am);
	print_binary64(bm);
	print_binary64(m);
	#endif
	return S_F32_create(s, e, (u32)(u64)m);
}
static inline u64 S_F32_wholeDown(s32 e, u64 m)
{
	// shift 23 right to get whole number
	int32_t shift=-23;
	shift+=e;
	if(shift<-23)
	{
		return 0;
	}else if(shift<0)
	{
		return (m>>-shift);
	}else if(shift>40)
	{
		return 0xFFFFFFFFFFFFFFFFll;
	}else
	{
		return (m<<shift);
	}
}
static inline u64 S_F32_wholeUp(s32 e, u64 m)
{
	return S_F32_wholeDown(e, m+(u64)(1<<23));
}
static inline s32 S_F32_trunc_to_s32(S_F32 a)
{
	s32 ae=S_F32_getExp(a);
	u32 as=S_F32_getSign(a);
	s32 mulSign=(as==0)?1:-1;
	u64 am=(u64)S_F32_getMantissa(a);
	am|=(((u64)1)<<(23));
	if(S_F32_isZero(a))
	{
		return 0;
	}
	return mulSign*(s32)S_F32_wholeDown(ae, am);
}
static inline s32 S_F32_floor_to_s32(S_F32 a)
{
	s32 ae=S_F32_getExp(a);
	u32 as=S_F32_getSign(a);
	s32 mulSign=(as==0)?1:-1;
	u64 am=(u64)S_F32_getMantissa(a);
	am|=(((u64)1)<<(23));
	if(S_F32_isZero(a))
	{
		return 0;
	}
	if(mulSign)
	{
		return mulSign*(s32)S_F32_wholeUp(ae, am);
	}else
	{
		return mulSign*(s32)S_F32_wholeDown(ae, am);
	}
}
static inline s32 S_F32_ceil_to_s32(S_F32 a)
{
	s32 ae=S_F32_getExp(a);
	u32 as=S_F32_getSign(a);
	s32 mulSign=(as==0)?1:-1;
	u64 am=(u64)S_F32_getMantissa(a);
	am|=(((u64)1)<<(23));
	if(S_F32_isZero(a))
	{
		return 0;
	}
	if(mulSign)
	{
		return mulSign*(s32)S_F32_wholeDown(ae, am);
	}else
	{
		return mulSign*(s32)S_F32_wholeUp(ae, am);
	}
}
static inline s32 S_F32_signum(S_F32 a)
{
	if(S_F32_isZero(a))
	{
		return 0;
	}
	u32 as=S_F32_getSign(a);
	return as?-1:1;
}
static inline s32 S_F32_compare(S_F32 a, S_F32 b)
{
	s32 sa=S_F32_signum(a);
	s32 sb=S_F32_signum(b);
	if(sa==0 && sb==0)
	{
		return 0;
	}
	if(sa!=sb)
	{
		return sa>sb?1:-1;
	}
	s32 greater;
	
	u32 as=S_F32_getSign(a);
	u32 bs=S_F32_getSign(b);

	s32 ae=S_F32_getExp(a);
	s32 be=S_F32_getExp(b);
	if(ae>be)
	{
//		printf("S_F32_compare exponent a>b\n");
		greater=1;
	}else if(be>ae)
	{
//		printf("S_F32_compare exponent a<b\n");
		greater=-1;
	}else
	{
		u64 am=(u64)S_F32_getMantissa(a);
		u64 bm=(u64)S_F32_getMantissa(b);
		greater=(am>bm)?1:((am==bm)?0:-1);
//		printf("S_F32_compare mantissa diff %d am: %lld bm: %lld\n", greater, am, bm);
	}
	return (sa==0)?greater:-greater;
}
static inline S_F32 S_F32_nextafter(S_F32 a, S_F32 b)
{
	s32 dir=S_F32_compare(a,b);
	if(dir==0)
	{
		return a;
	}
	u32 s=S_F32_getSign(a);
	bool inc=(dir>0) != (s==0);
	u64 m=S_F32_getMantissa(a);
	s32 e=S_F32_getExp(a);
	m|=(((u64)1)<<(23));
	if(inc)
	{
		m++;
		if((m&(((u64)1)<<(24)))!=0)
		{
			m>>=1;
			e++;
		}
	}else
	{
		m--;
		if((m&(((u64)1)<<(22)))==0)
		{
			m<<=1;
			e--;
		}
	}
	// TODO this implementation does not handle all cases yet.
	return S_F32_create(s, e, (u32)(u64)m);
}
#endif
#ifdef SOFTFLOAT_LIB_TEST
#ifndef SOFTFLOAT_LIB_TEST_H_
#define SOFTFLOAT_LIB_TEST_H_
static inline void S_F32_TEST_assertEq(S_F32 v, float expv)
{
	float fv=S_F32_toFloat(v);
	if(fv!=expv)
	{
		printf("S_F32_TEST_assertEq error %f != %f\n", fv, expv);
		exit(1);
	}
}
static inline S_F32 S_F32_div_TEST(S_F32 a, S_F32 b)
{
	S_F32 ret=S_F32_div(a, b);
	printf("%f/%f\n", S_F32_toFloat(a), S_F32_toFloat(b));
	S_F32_TEST_assertEq(ret, S_F32_toFloat(a)/S_F32_toFloat(b));
	return ret;
}
#warning TEST_ASDASDASDASDAD
#endif
#endif

