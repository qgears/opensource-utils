// jawt.so must exist so that our current lwjgl version can init itself. But it need not have any content
// in case we do not use LWJGL window only the OpenGL API to access OSMesa, or KMS+OpenGL
// This file compiles into an empty libjawt.so
