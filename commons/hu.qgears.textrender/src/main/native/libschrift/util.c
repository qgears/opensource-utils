#include "util.h"

#include <stddef.h>

const char *filename(const char *str)
{
    if (str == NULL) {
        return NULL;
    }

    const char *last = str;

    for (const char *p = str; *p != '\0'; p++) {
        if (*p == '/') {
            last = p+1;
        }
    }

    return last;  // if not found → original str
}
