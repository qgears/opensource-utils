#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
typedef uint32_t u32;
typedef int32_t s32;
typedef uint64_t u64;
typedef int64_t s64;

void print_binary(uint32_t x)
{
    for (int i = 31; i >= 0; --i) {
        printf("%u", (x >> i) & 1);

        if (i % 8 == 0 && i != 0)
            printf("  ");
    }

    printf("\n");
}
void print_binary64(uint64_t x)
{
    for (int i = 63; i >= 0; --i) {
        printf("%u", (u32)((x >> i) & 1));

        if (i % 8 == 0 && i != 0)
            printf("  ");
    }

    printf("\n");
}

#define SOFTFLOAT_IMPL 1
#include "softfloat.h"

static void compare(float a, float b)
{
	printf("%f ? %f = %d\n", a, b, S_F32_compare(S_F32_fromFloat(a),S_F32_fromFloat(b)));
}

int main(int argc, char ** argv)
{
	S_F32 one=S_F32_create(0, 0, 0);
	S_F32 one_c = S_F32_fromFloat(1.0f);
	S_F32 twoD1 = S_F32_fromFloat(2.1f);
	print_binary(one);
	printf("Value: %d %f\n", one,  S_F32_toFloat(one));
	if(one!=one_c)
	{
		printf("ERROR\n");
	}
	S_F32 two = S_F32_add(one, one);
	printf("1+1=%f\n", S_F32_toFloat(two));
	printf("1*1=%f\n", S_F32_toFloat(S_F32_mul(one, one)));
	printf("2*2=%f\n", S_F32_toFloat(S_F32_mul(two, two)));
	printf("2.1*2.1=%f %f\n", S_F32_toFloat(S_F32_mul(twoD1, twoD1)), 2.1f*2.1f);
	
	printf("1/1=%f\n", S_F32_toFloat(S_F32_div(one, one)));
	printf("1/2=%f\n", S_F32_toFloat(S_F32_div(one, two)));
	printf("2/1=%f\n", S_F32_toFloat(S_F32_div(two, one)));
	printf("1.0f/0.0f=%f\n", 1.0f/0.0f);
	printf("(u32)0.9=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(0.9f)));
	printf("(u32)1.0=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(1.0f)));
	printf("(u32)1.5=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(1.5f)));
	printf("(u32)2.0=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(2.0f)));
	printf("(u32)-0.9=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(-0.9f)));
	printf("(u32)-1.0=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(-1.0f)));
	printf("(u32)-1.5=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(-1.5f)));
	printf("(u32)-2.0=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(-2.0f)));
	printf("(u32)-2.5=%d\n", S_F32_trunc_to_s32(S_F32_fromFloat(-2.5f)));
	for(int32_t i=-20; i<21;++i)
	{
		printf("%d=%f signum: %d\n",i,S_F32_toFloat(S_F32_from_s64(i)),S_F32_signum(S_F32_from_s64(i)));
	}
	compare(0.0f, 0.0f);
	compare(0.0001f, 0.0f);
	compare(-0.0001f, 0.0f);
	compare(-0.0001f, 0.0001f);
	compare(0.0001f, -0.0001f);
	compare(-0.0001f, -0.00011f);
	compare(-0.0001f, -0.00009f);
	return 0;
}

