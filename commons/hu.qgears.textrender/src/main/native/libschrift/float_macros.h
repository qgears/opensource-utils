#ifndef SOFTFLOAT_H_
#define SOFTFLOAT_H_

#include <stdbool.h>
#include <math.h>

#ifdef SOFTFLOAT
#include "float_macros_softfloat.h"
#else
	#ifndef USE_DOUBLE
		#include "float_macros_original_32.h"
	#else
		#include "float_macros_original.h"
	#endif
#endif

#endif

