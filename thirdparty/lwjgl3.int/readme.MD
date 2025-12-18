= LWJGL 3 integration project

This is a compatibility layer to provide LWJGL2 APIS we are interested in over LWJGL3
 
== Custom built LWJGL 3 (3.3.6) binaries for linux i386


The project also shares linux i386 binaries. x86 linux binaries are not released by LWJGL team, but the build scripts still work for this platform.

In this project we share the core libs built by us.

The libs are placed on buildpath with a proper directory structure : during the build they will be copied to the classpath where LWJGL looks for them by default, so runtime the libs can be found and loaded without any lib path configuration.

=== LWJGL3 source build 

 * Clone GIT : git@github.com:LWJGL/lwjgl3.git
 ** use tag 3.3.6
 * Generate templates on host with 64bit java (sadly an OOM error occurs with 32bit java in docker container)
 ** `ant compile-templates`
 * Setup cross compile using docker
 * debian12-slim i386 as base image : compilation will produce GLIBC 3.34 compatible *.so-s. If you use debian13, then GLIBC 3.38 will be a runtime dependency, which requires an up-to-date OS (ubuntu 22 is not compatible!) 
 * install build deps : install -y ant libgtk-3-dev 
 ** NOTE: I used our internal xcomp-debian12-i386 image as base, where essential build tools are already installed. 
 * offline build (prevents trials to access LWJGL CI server) : LWJGL_BUILD_OFFLINE=true
 * Copy libffi.a manually to its place (normally this file is downloaded from LWJGL CI, which is inaccessible for us)
 ** `mkdir -p "${LWJGL_BUILD_OUTPUT}/libs/native/linux/x86/org/lwjgl/ && cp /usr/lib/i386-linux-gnu/libffi.a "${LWJGL_BUILD_OUTPUT}/libs/native/linux/x86/org/lwjgl"` 
 * Compile LWJGL core components :
 ** ant -Dbinding.meow=false -Dbinding.nfd=false compile-native
 ** find results in bin/libs/native
 
Script to summarize steps (run from LWJGL root directory)
 
#!/bin/bash
export LWJGL_BUILD_OFFLINE=true
rm -fr bin/linux/ bin/libs/native/
mkdir -p bin/libs/native/linux/x86/org/lwjgl
cp /usr/lib/i386-linux-gnu/libffi.a bin/libs/native/linux/x86/org/lwjgl
ant -Dbinding.meow=false -Dbinding.nfd=false compile-native
 
== GLFW source build

GLFW is forked by LWJGL team, use this git for build : git@github.com:LWJGL-CI/glfw.git
* No tag for build, I used latest master

`
commit ce62ff92ed6a395ecc936b6c17d2dc7ac56b1d8e (HEAD -> master, origin/master, origin/HEAD)
Author: Ioannis Tsakpinis <iotsakp@gmail.com>
Date:   Fri Feb 26 21:30:36 2021 +0200

    LWJGL CI configuration
`

Can be built in same docker container as we used for LWJGL, but further deps need to be installed :

 * apt install libwayland-dev libxkbcommon-dev xorg-dev cmake 
 * Run CMAKE: `cmake -S . -B build -D BUILD_SHARED_LIBS=ON`
 * build so : `make -C build`
 * result is build/src/libglfw.so
    
   