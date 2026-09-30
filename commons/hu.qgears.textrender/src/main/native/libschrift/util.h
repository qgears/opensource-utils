#ifndef UTIL_H
#define UTIL_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Returns the file name part of a '/' separated path.
 *
 * @param str The path to process. May be NULL.
 *
 * @return Pointer into str right after the last '/', or str itself if it
 *         contains no '/'. NULL if str is NULL.
 */
const char *filename(const char *str);

#ifdef __cplusplus
}
#endif

#endif /* UTIL_H */
